package org.tbk.nostr.template;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@Slf4j
public class SimpleNostrTemplate implements NostrTemplate {

    private final RelayUri relay;

    private final WebSocketClient webSocketClient;

    private final WebSocketHttpHeaders headers;

    public SimpleNostrTemplate(RelayUri relay) {
        this.relay = requireNonNull(relay);
        this.webSocketClient = new StandardWebSocketClient();
        this.headers = new WebSocketHttpHeaders();
    }

    @Override
    public Mono<Event> fetchEventById(EventId id) {
        return fetchEventsByIds(Collections.singletonList(id)).next();
    }

    @Override
    public Flux<Event> fetchEventsByIds(List<EventId> ids) {
        if (ids.isEmpty()) {
            return Flux.empty();
        }

        Set<ByteString> idsAsBytes = ids.stream()
                .map(it -> ByteString.copyFrom(it.toByteArray()))
                .collect(Collectors.toSet());

        SubscriptionId subscriptionId = createUniqueSubscriptionId(idsAsBytes);

        return fetchEvents(ReqRequest.newBuilder()
                .setId(subscriptionId.getId())
                .addFilters(Filter.newBuilder()
                        .addAllIds(idsAsBytes)
                        .build())
                .build());
    }

    @Override
    public Flux<Event> fetchEventByAuthor(XonlyPublicKey publicKey) {
        return fetchEventsByAuthors(Collections.singletonList(publicKey));
    }

    @Override
    public Flux<Event> fetchEventsByAuthors(List<XonlyPublicKey> publicKeys) {
        if (publicKeys.isEmpty()) {
            return Flux.empty();
        }

        Set<ByteString> idsAsBytes = publicKeys.stream()
                .map(it -> ByteString.copyFrom(it.value.toByteArray()))
                .collect(Collectors.toSet());

        SubscriptionId subscriptionId = createUniqueSubscriptionId(idsAsBytes);

        return fetchEvents(ReqRequest.newBuilder()
                .setId(subscriptionId.getId())
                .addFilters(Filter.newBuilder()
                        .addAllAuthors(idsAsBytes)
                        .build())
                .build());
    }

    @Override
    public Mono<Metadata> fetchMetadataByAuthor(XonlyPublicKey publicKey) {
        Set<ByteString> idsAsBytes = Set.of(publicKey).stream()
                .map(it -> ByteString.fromHex(publicKey.value.toHex()))
                .collect(Collectors.toSet());

        SubscriptionId subscriptionId = createUniqueSubscriptionId(idsAsBytes);

        return fetchEvents(ReqRequest.newBuilder()
                .setId(subscriptionId.getId())
                .addFilters(Filter.newBuilder()
                        .addKinds(0)
                        .addAllAuthors(idsAsBytes)
                        .build())
                .build())
                .map(it -> JsonReader.fromJsonMetadata(it.getContent()))
                .next();
    }

    @Override
    public Flux<Event> fetchEvents(ReqRequest request) {
        AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>();
        return Flux.<Event>create(sink -> {
            try {
                sessionRef.set(webSocketClient.execute(new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        sink.complete();
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                        log.debug("handleTextMessage: {}", message.getPayload());

                        try {
                            Response response = JsonReader.fromJsonResponse(message.getPayload());
                            switch (response.getKindCase()) {
                                case EVENT -> {
                                    EventResponse eventResponse = response.getEvent();
                                    if (!eventResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        Event event = eventResponse.getEvent();
                                        if (MoreEvents.isValid(event)) {
                                            sink.next(event);
                                        } else {
                                            sink.error(new IllegalArgumentException("Invalid event data"));
                                        }
                                    }
                                }
                                case EOSE -> {
                                    EoseResponse eoseResponse = response.getEose();
                                    if (!eoseResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        sink.complete();
                                    }
                                }
                                case CLOSED -> {
                                    ClosedResponse closedResponse = response.getClosed();
                                    if (!closedResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        sink.complete();
                                    }
                                }
                                case OK, NOTICE, COUNT, KIND_NOT_SET -> {
                                    log.warn("Unexpected message received (type := {}). Ignoring.", response.getKindCase());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error in handleTextMessage while handling '{}': {}", message, e.getMessage());
                            sink.error(e);
                        }
                    }
                }, headers, relay.getUri()).get());

                TextMessage message = new TextMessage(JsonWriter.toJson(request));

                log.debug("Sending message: {}", message.getPayload());
                sessionRef.get().sendMessage(message);
            } catch (InterruptedException | ExecutionException | IOException e) {
                sink.error(e);
            }
        }).doFinally(signalType -> {
            log.debug("Closing websocket session on signal type: {}", signalType);
            closeQuietly(sessionRef.get());
        });
    }

    @Override
    public Flux<CountResult> countEvents(CountRequest request) {

        AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>();
        return Flux.<CountResult>create(sink -> {
            try {
                sessionRef.set(webSocketClient.execute(new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        sink.complete();
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                        log.debug("handleTextMessage: {}", message.getPayload());

                        try {
                            Response response = JsonReader.fromJsonResponse(message.getPayload());
                            switch (response.getKindCase()) {
                                case COUNT -> {
                                    CountResponse countResponse = response.getCount();
                                    if (!countResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        sink.next(countResponse.getResult());
                                    }
                                }
                                case EOSE -> {
                                    EoseResponse eoseResponse = response.getEose();
                                    if (!eoseResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        sink.complete();
                                    }
                                }
                                case CLOSED -> {
                                    ClosedResponse closedResponse = response.getClosed();
                                    if (!closedResponse.getSubscriptionId().equals(request.getId())) {
                                        log.warn("{} on unexpected subscription received. Ignoring.", response.getKindCase());
                                    } else {
                                        sink.complete();
                                    }
                                }
                                case OK, NOTICE, EVENT, KIND_NOT_SET -> {
                                    log.warn("Unexpected message received (type := {}). Ignoring.", response.getKindCase());
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error in handleTextMessage while handling '{}': {}", message, e.getMessage());
                            sink.error(e);
                        }
                    }
                }, headers, relay.getUri()).get());

                TextMessage message = new TextMessage(JsonWriter.toJson(request));

                log.debug("Sending message: {}", message.getPayload());
                sessionRef.get().sendMessage(message);
            } catch (InterruptedException | ExecutionException | IOException e) {
                sink.error(e);
            }
        }).doFinally(signalType -> {
            log.debug("Closing websocket session on signal type: {}", signalType);
            closeQuietly(sessionRef.get());
        });
    }

    @Override
    public Mono<OkResponse> send(Event event) {
        AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>();

        return Mono.<OkResponse>create(sink -> {
            try {
                sessionRef.set(webSocketClient.execute(new TextWebSocketHandler() {
                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
                        sink.success();
                    }

                    @Override
                    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                        log.debug("handleTextMessage: {}", message.getPayload());

                        try {
                            Response response = JsonReader.fromJsonResponse(message.getPayload());
                            if (response.getKindCase() == Response.KindCase.OK) {
                                OkResponse ok = response.getOk();
                                if (Arrays.equals(event.getId().toByteArray(), ok.getEventId().toByteArray())) {
                                    sink.success(ok);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("Error in handleTextMessage while handling '{}': {}", message, e.getMessage());
                            sink.error(e);
                        }
                    }
                }, headers, relay.getUri()).get());

                EventRequest req = EventRequest.newBuilder()
                        .setEvent(event)
                        .build();

                TextMessage message = new TextMessage(JsonWriter.toJson(req));

                log.debug("Sending message: {}", message.getPayload());
                sessionRef.get().sendMessage(message);
            } catch (InterruptedException | ExecutionException | IOException e) {
                sink.error(e);
            }
        }).doFinally(signalType -> {
            log.debug("Closing websocket session on signal type: {}", signalType);
            closeQuietly(sessionRef.get());
        });
    }


    private static SubscriptionId createUniqueSubscriptionId(Set<ByteString> list) {
        return createUniqueSubscriptionId(list.stream()
                .map(ByteString::toByteArray)
                .toList());
    }

    private static SubscriptionId createUniqueSubscriptionId(List<byte[]> list) {
        return SubscriptionId.of(
                HexFormat.of().formatHex(Crypto.sha256(
                        HexFormat.of().parseHex(list.stream()
                                .map(it -> HexFormat.of().formatHex(it))
                                .collect(Collectors.joining()))))
        );
    }

    private static void closeQuietly(@Nullable WebSocketSession session) {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (Exception e) {
                log.warn("Error while closing websocket session: {}", e.getMessage());
            }
        }
    }

}
