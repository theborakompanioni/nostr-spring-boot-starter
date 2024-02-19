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
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
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
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private final StandardWebSocketClient client;

    private volatile WebSocketSession session;

    private final SubmissionPublisher<TextMessage> publisher = new SubmissionPublisher<>(publisherExecutor, Flow.defaultBufferSize());

    private final TextWebSocketHandler textWebSocketHandler = new SubmissionPublisherTextWebSocketHandler(this.publisher);

    private final Map<SubscriptionId, SubscribeContext> subscriptions = new ConcurrentHashMap<>();

    @Builder
    private record SubscribeContext(@NonNull ReqRequest reqRequest,
                                    @NonNull NostrClientService.SubscribeOptions options) {
    }

    public SimpleNostrClientService(RelayUri relay) {
        this.relayUri = requireNonNull(relay);
        this.client = new StandardWebSocketClient();
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
        return Scheduler.newFixedDelaySchedule(Duration.ofSeconds(30), Duration.ofSeconds(30));
    }

    @Override
    protected void runOneIteration() throws Exception {
        if (session != null && session.isOpen()) {
            log.trace("Sending ping to {}", relayUri);
            this.session.sendMessage(new PingMessage());
        }
    }

    @Override
    protected void startUp() throws ExecutionException, InterruptedException {
        log.info("Trying to connect to relay {}", relayUri);

        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        this.session = client.execute(textWebSocketHandler, headers, relayUri.getUri())
                .thenApply(it -> new ConcurrentWebSocketSessionDecorator(it, (int) Duration.ofSeconds(60).toMillis(), 512 * 1024))
                .get();

        log.info("Successfully connected to relay {}", relayUri);
    }

    @Override
    protected void shutDown() {
        log.info("Closing {} subscriptions on relay {}", this.subscriptions.size(), this.relayUri.getUri());

        Set<SubscriptionId> subscriptionIds = Set.copyOf(subscriptions.keySet());
        for (SubscriptionId id : subscriptionIds) {
            this.sendClose(id);
            subscriptions.remove(id);
        }

        if (this.session != null) {
            log.info("Shutting down connection to relay {}", this.relayUri.getUri());
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
            log.trace("Successfully connected! Headers: {}", session.getHandshakeHeaders());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) {
            log.trace("handleTextMessage: {}", message.getPayload());
            if (publisher.isClosed()) {
                log.warn("Will not submit incoming websocket message: Publisher is already closed.");
            } else {
                publisher.submit(message);
            }
        }
    }
}
