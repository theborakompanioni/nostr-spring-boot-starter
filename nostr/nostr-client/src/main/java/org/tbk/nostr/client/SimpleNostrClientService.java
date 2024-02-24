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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleNostrClientService extends AbstractScheduledService implements NostrClientService {
    private final String serviceId = Integer.toHexString(System.identityHashCode(this));

    private final ExecutorService publisherExecutor = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("nostr-msg-pub-" + serviceId + "-%d")
            .setDaemon(false)
            .build());

    private final RelayUri relayUri;

    private final WebSocketClient client;

    private final SubmissionPublisher<TextMessage> publisher;

    private final TextWebSocketHandler textWebSocketHandler;

    private final Map<SubscriptionId, SubscribeContext> subscriptions;

    private final Scheduler heartbeatScheduler;

    private volatile WebSocketSession session;

    @Builder
    private record SubscribeContext(@NonNull ReqRequest reqRequest,
                                    @NonNull NostrClientService.SubscribeOptions options) {
    }

    public SimpleNostrClientService(RelayUri relay) {
        this(relay, new StandardWebSocketClient());
    }

    public SimpleNostrClientService(RelayUri relay, WebSocketClient webSocketClient) {
        this(relay, webSocketClient, Duration.ofSeconds(30));
    }

    public SimpleNostrClientService(RelayUri relay, WebSocketClient webSocketClient, Duration heartbeatInterval) {
        this.relayUri = requireNonNull(relay);
        this.client = requireNonNull(webSocketClient);

        this.publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());
        this.textWebSocketHandler = new SubmissionPublisherTextWebSocketHandler(this.publisher);
        this.subscriptions = new ConcurrentHashMap<>();

        this.heartbeatScheduler = Scheduler.newFixedDelaySchedule(heartbeatInterval, heartbeatInterval);
    }


    @Override
    public Flux<Event> subscribe(ReqRequest req, SubscribeOptions options) {
        verifySession();

        SubscriptionId subscriptionId = SubscriptionId.of(req.getId());

        return this.connect(subscriptionId).doOnSubscribe(s -> {
            this.subscriptions.put(subscriptionId, SubscribeContext.builder()
                    .reqRequest(req)
                    .options(options)
                    .build());

            this.send(Request.newBuilder()
                    .setReq(req)
                    .build());
        });
    }

    @Override
    public Flux<Event> connect(SubscriptionId id) {
        verifySession();

        return Flux.<TextMessage>from(s -> publisher.subscribe(FlowAdapters.toFlowSubscriber(s)))
                .map(it -> JsonReader.fromJson(it.getPayload(), Response.newBuilder()))
                .handle(filterSubscriptionResponses(id))
                .handle((it, sink) -> {
                    switch (it.getKindCase()) {
                        case CLOSED -> {
                            subscriptions.remove(id);
                            sink.complete();
                        }
                        case EOSE -> {
                            Optional.ofNullable(subscriptions.get(id))
                                    .filter(ctx -> ctx.options.isCloseOnEndOfStream())
                                    .ifPresent(ctx -> {
                                        SubscribeContext removed = subscriptions.remove(id);
                                        if (removed != null) {
                                            sendClose(id);
                                        }

                                        sink.complete();
                                    });
                        }
                        case EVENT -> {
                            Event event = it.getEvent().getEvent();
                            if (MoreEvents.hasValidSignature(event)) {
                                sink.next(event);
                            }
                        }
                        default -> {
                            // do nothing on purpose
                        }
                    }
                });
    }

    @NonNull
    private static BiConsumer<Response, SynchronousSink<Response>> filterSubscriptionResponses(SubscriptionId id) {
        return (it, sink) -> {
            switch (it.getKindCase()) {
                case CLOSED -> {
                    SubscriptionId subscriptionId = SubscriptionId.of(it.getClosed().getSubscriptionId());
                    if (subscriptionId.equals(id)) {
                        sink.next(it);
                    }
                }
                case EOSE -> {
                    SubscriptionId subscriptionId = SubscriptionId.of(it.getEose().getSubscriptionId());
                    if (subscriptionId.equals(id)) {
                        sink.next(it);
                    }
                }
                case EVENT -> {
                    SubscriptionId subscriptionId = SubscriptionId.of(it.getEvent().getSubscriptionId());
                    if (subscriptionId.equals(id)) {
                        sink.next(it);
                    }
                }
                default -> {
                    // do nothing on purpose
                }
            }
        };
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

    private void sendClose(SubscriptionId id) {
        this.send(Request.newBuilder()
                .setClose(CloseRequest.newBuilder()
                        .setId(id.getId())
                        .build())
                .build());
    }

    private void send(Request request) {
        verifySession();

        try {
            TextMessage message = new TextMessage(JsonWriter.toJson(request));
            log.debug("Sending {}", message.getPayload());
            this.session.sendMessage(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void verifySession() {
        checkState(this.session.isOpen(), "Session must be open");
    }

    @Override
    protected Scheduler scheduler() {
        return this.heartbeatScheduler;
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (session != null && session.isOpen()) {
            ByteBuffer payload = ByteBuffer.allocate(Long.BYTES).putLong(0, System.currentTimeMillis() / 1_000L);
            log.trace("Sending ping to {}: {}", session.getRemoteAddress(), HexFormat.of().formatHex(payload.array()));
            this.session.sendMessage(new PingMessage(payload));
        }
    }

    @Override
    protected void startUp() throws ExecutionException, InterruptedException {
        log.info("Trying to connect to relay {}", relayUri.getUri());

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        this.session = client.execute(textWebSocketHandler, headers, relayUri.getUri())
                .thenApply(it -> new ConcurrentWebSocketSessionDecorator(it, (int) Duration.ofSeconds(60).toMillis(), 512 * 1024))
                .get();

        log.info("Successfully connected to relay {}", relayUri.getUri());
    }

    @Override
    protected void shutDown() {
        log.info("Closing {} subscriptions on relay {}", this.subscriptions.size(), this.relayUri.getUri());

        if (this.session != null) {
            log.info("Shutting down connection to relay {}", this.relayUri.getUri());

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

            try {
                this.session.close();
            } catch (Exception e) {
                log.warn("Error while closing session: {}", e.getMessage());
            }
        }

        publisher.close();

        boolean success = MoreExecutors.shutdownAndAwaitTermination(this.publisherExecutor, Duration.ofSeconds(60));
        if (!success) {
            log.warn("Could not cleanly shutdown publisher executor");
        }
    }

    @VisibleForTesting
    WebSocketSession getSession() {
        return this.session;
    }

    @VisibleForTesting
    Map<SubscriptionId, SubscribeContext> getSubscriptions() {
        return Collections.unmodifiableMap(this.subscriptions);
    }

    @RequiredArgsConstructor
    private static class SubmissionPublisherTextWebSocketHandler extends TextWebSocketHandler {
        @NonNull
        private final SubmissionPublisher<TextMessage> publisher;

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.debug("Connection established to {}: {}", session.getRemoteAddress(), session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            log.trace("handleTextMessage: message with {} bytes", message.getPayloadLength());
            if (publisher.isClosed()) {
                log.warn("Will not submit incoming websocket message: Publisher is already closed.");
            } else {
                publisher.submit(message);
            }
        }

        @Override
        protected void handlePongMessage(WebSocketSession session, PongMessage message) {
            log.trace("handlePongMessage: {}", HexFormat.of().formatHex(message.getPayload().array()));
        }

        @Override
        public void handleTransportError(WebSocketSession session, Throwable t) {
            log.error("handleTransportError", t);
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
            log.debug("Connection closed to {}: {}", session.getRemoteAddress(), status);
        }
    }
}
