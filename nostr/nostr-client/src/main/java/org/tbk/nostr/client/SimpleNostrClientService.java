package org.tbk.nostr.client;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractScheduledService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.FlowAdapters;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleNostrClientService extends AbstractScheduledService implements NostrClientService {
    private final static OnCloseHandler defaultOnCloseHandler = new ReconnectOnClose();
    private final static Duration defaultHeartbeatInterval = Duration.ofSeconds(30);

    private final String serviceId = Integer.toHexString(System.identityHashCode(this));

    private final ScheduledExecutorService publisherExecutor = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder()
            .setNameFormat("nostr-msg-pub-" + serviceId + "-%d")
            .setDaemon(false)
            .build());

    private final RelayUri relayUri;

    private final WebSocketClient client;

    private final OnCloseHandler onCloseHandler;

    private final SubmissionPublisher<TextMessage> publisher;

    private final TextWebSocketHandler textWebSocketHandler;

    private final Map<SubscriptionId, SubscriptionContext> subscriptions;

    private final Scheduler heartbeatScheduler;

    private volatile WebSocketSession session;

    @Builder
    private record SubscriptionContext(@NonNull ReqRequest reqRequest,
                                       @NonNull NostrClientService.SubscribeOptions options) {
    }

    public SimpleNostrClientService(RelayUri relayUri) {
        this(relayUri, new StandardWebSocketClient());
    }

    public SimpleNostrClientService(RelayUri relayUri,
                                    WebSocketClient webSocketClient) {
        this(relayUri, webSocketClient, defaultHeartbeatInterval);
    }

    public SimpleNostrClientService(RelayUri relayUri,
                                    WebSocketClient webSocketClient,
                                    Duration heartbeatInterval) {
        this(relayUri, webSocketClient, heartbeatInterval, defaultOnCloseHandler);
    }

    public SimpleNostrClientService(RelayUri relayUri,
                                    WebSocketClient webSocketClient,
                                    Duration heartbeatInterval,
                                    OnCloseHandler onCloseHandler) {
        this.relayUri = requireNonNull(relayUri);
        this.client = requireNonNull(webSocketClient);
        this.onCloseHandler = requireNonNull(onCloseHandler);

        this.heartbeatScheduler = Scheduler.newFixedDelaySchedule(requireNonNull(heartbeatInterval), heartbeatInterval);

        this.publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());
        this.textWebSocketHandler = new SubmissionPublisherTextWebSocketHandler(this.publisher, this::onConnectionClosed);
        this.subscriptions = new ConcurrentHashMap<>();
    }

    @Override
    public RelayUri getRelayUri() {
        return this.relayUri;
    }

    @Override
    public Flux<Event> subscribe(ReqRequest req, SubscribeOptions options) {
        checkOpenSession();

        SubscriptionId subscriptionId = SubscriptionId.of(req.getId());

        return this.attachToEvents(subscriptionId).doOnSubscribe(s -> {
            this.subscriptions.put(subscriptionId, SubscriptionContext.builder()
                    .reqRequest(req)
                    .options(options)
                    .build());

            this.send(Request.newBuilder()
                    .setReq(req)
                    .build());
        });
    }

    @Override
    public Flux<Event> attachToEvents(SubscriptionId id) {
        return attachTo(id)
                .filter(it -> Response.KindCase.EVENT.equals(it.getKindCase()))
                .map(Response::getEvent)
                .map(EventResponse::getEvent)
                .filter(MoreEvents::hasValidSignature);
    }

    @Override
    public Flux<Response> attachTo(SubscriptionId id) {
        return attach()
                .handle(filterSubscriptionResponses(id))
                .handle((it, sink) -> {
                    switch (it.getKindCase()) {
                        case CLOSED -> {
                            subscriptions.remove(id);
                            sink.next(it);
                            sink.complete();
                        }
                        case EOSE -> {
                            SubscriptionContext ctx = subscriptions.get(id);

                            if (ctx != null && ctx.options.isCloseOnEndOfStream()) {
                                SubscriptionContext removed = subscriptions.remove(id);
                                if (removed != null) {
                                    sendClose(id);
                                }
                                sink.next(it);
                                sink.complete();
                            } else {
                                sink.next(it);
                            }
                        }
                        default -> sink.next(it);
                    }
                });
    }

    @Override
    public Flux<Response> attach() {
        checkOpenSession();

        return Flux.<TextMessage>from(s -> publisher.subscribe(FlowAdapters.toFlowSubscriber(s)))
                .map(it -> JsonReader.fromJson(it.getPayload(), Response.newBuilder()));
    }

    @Override
    public Mono<Void> close(SubscriptionId id) {
        return Mono.fromCallable(() -> {
            sendClose(id);
            subscriptions.remove(id);
            return null;
        });
    }

    @Override
    public Mono<Void> send(Event event) {
        return Mono.fromCallable(() -> {
            this.send(Request.newBuilder()
                    .setEvent(EventRequest.newBuilder()
                            .setEvent(event)
                            .build())
                    .build());
            return null;
        });
    }

    @Override
    public Mono<Void> auth(Event event) {
        return Mono.fromCallable(() -> {
            this.send(Request.newBuilder()
                    .setAuth(AuthRequest.newBuilder()
                            .setEvent(event)
                            .build())
                    .build());
            return null;
        });
    }

    @Override
    public boolean isConnected() {
        return this.session != null && this.session.isOpen();
    }

    @Override
    public Mono<Boolean> reconnect(Duration delay) {
        if (publisherExecutor.isShutdown()) {
            return Mono.just(false);
        }

        return Mono.fromCallable(() -> {
            if (publisherExecutor.isShutdown()) {
                return false;
            }

            State serviceState = this.state();
            if (serviceState != State.RUNNING) {
                log.warn("Not reconnecting as service is in state {}", serviceState);
                return false;
            }

            closeSession();
            openSession();
            resubscribe();

            return true;
        }).delaySubscription(delay, Schedulers.fromExecutor(publisherExecutor));
    }

    @Override
    protected Scheduler scheduler() {
        return this.heartbeatScheduler;
    }

    @Override
    protected void runOneIteration() {
        if (session != null && session.isOpen()) {
            log.trace("Sending ping to {}", session.getRemoteAddress());
            try {
                this.session.sendMessage(new PingMessage());
            } catch (IOException e) {
                log.warn("Error while sending ping message: {}", e.getMessage());
            }
        }
    }

    @Override
    protected void startUp() throws ExecutionException, InterruptedException {
        openSession();
    }

    @Override
    protected void shutDown() {
        log.info("Closing {} subscriptions on relay {}", this.subscriptions.size(), this.relayUri.getUri());

        closeSubscriptions();
        closeSession();

        publisher.close();

        boolean success = MoreExecutors.shutdownAndAwaitTermination(this.publisherExecutor, Duration.ofSeconds(60));
        if (!success) {
            log.warn("Could not cleanly shutdown publisher executor");
        }
    }

    private void checkOpenSession() {
        checkState(this.session.isOpen(), "Session must be open");
    }

    private void onConnectionClosed(CloseStatus closeStatus) {
        State serviceState = this.state();
        if (serviceState != State.RUNNING) {
            log.trace("Not calling OnCloseHandler as service is in state {}", serviceState);
            return;
        }

        this.onCloseHandler.doOnClose(this, closeStatus);
    }

    private void sendClose(SubscriptionId id) {
        this.send(Request.newBuilder()
                .setClose(CloseRequest.newBuilder()
                        .setId(id.getId())
                        .build())
                .build());
    }

    private void send(Request request) {
        checkOpenSession();

        try {
            TextMessage message = new TextMessage(JsonWriter.toJson(request));
            log.debug("Sending {}", message.getPayload());
            this.session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resubscribe() {
        for (SubscriptionContext ctx : this.subscriptions.values()) {
            this.send(Request.newBuilder()
                    .setReq(ctx.reqRequest())
                    .build());
        }
    }

    private void openSession() throws InterruptedException, ExecutionException {
        log.info("Trying to connect to relay {}", relayUri.getUri());

        if (this.session != null && this.session.isOpen()) {
            throw new IllegalStateException("Session is already present");
        }

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        this.session = client.execute(textWebSocketHandler, headers, relayUri.getUri())
                .thenApply(it -> new ConcurrentWebSocketSessionDecorator(it, (int) Duration.ofSeconds(60).toMillis(), 512 * 1024))
                .get();

        log.info("Successfully connected to relay {}", relayUri.getUri());
    }

    private void closeSession() {
        if (this.session != null && this.session.isOpen()) {
            log.info("Closing session to relay {}", this.relayUri.getUri());

            try {
                this.session.close();
            } catch (Exception e) {
                log.warn("Error while closing session: {}", e.getMessage());
            }
        }
    }

    private void closeSubscriptions() {
        if (this.session != null) {
            if (this.session.isOpen()) {
                Set<SubscriptionId> subscriptionIds = Set.copyOf(subscriptions.keySet());
                for (SubscriptionId id : subscriptionIds) {
                    try {
                        this.sendClose(id);
                        subscriptions.remove(id);
                    } catch (Exception e) {
                        log.warn("Error while closing subscription {}: {}", id.getId(), e.getMessage());
                    }
                }
            }
        }
    }

    @VisibleForTesting
    WebSocketSession getSession() {
        return this.session;
    }

    @VisibleForTesting
    Map<SubscriptionId, SubscriptionContext> getSubscriptions() {
        return Collections.unmodifiableMap(this.subscriptions);
    }

    @RequiredArgsConstructor
    private static class SubmissionPublisherTextWebSocketHandler extends TextWebSocketHandler {
        @NonNull
        private final SubmissionPublisher<TextMessage> publisher;

        @NonNull
        private final Consumer<CloseStatus> onConnectionClosed;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.debug("Connection established to {}: {}", session.getRemoteAddress(), session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            log.trace("handleTextMessage with {} bytes: '{}'", message.getPayloadLength(), message.getPayload());
            if (publisher.isClosed()) {
                log.warn("Will not submit incoming websocket message: Publisher is already closed.");
            } else {
                publisher.submit(message);
            }
        }

        @Override
        protected void handlePongMessage(WebSocketSession session, PongMessage message) {
            log.trace("handlePongMessage: {} bytes", message.getPayloadLength());
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable t) {
            log.error("handleTransportError", t);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.debug("Connection closed to {}: {}", session.getRemoteAddress(), status);
            onConnectionClosed.accept(status);
        }
    }

    private static BiConsumer<Response, SynchronousSink<Response>> filterSubscriptionResponses(SubscriptionId subscriptionId) {
        return (it, sink) -> {
            boolean apply = Optional.ofNullable(switch (it.getKindCase()) {
                        case CLOSED -> it.getClosed().getSubscriptionId();
                        case EOSE -> it.getEose().getSubscriptionId();
                        case EVENT -> it.getEvent().getSubscriptionId();
                        case COUNT -> it.getCount().getSubscriptionId();
                        default -> null;
                    })
                    .map(subId -> subId.equals(subscriptionId.getId()))
                    .orElse(true);

            if (apply) {
                sink.next(it);
            }
        };
    }
}
