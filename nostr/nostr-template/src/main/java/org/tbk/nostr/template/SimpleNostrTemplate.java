package org.tbk.nostr.template;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
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
    public RelayUri getRelayUri() {
        return this.relay;
    }

    @Override
    public Mono<RelayInfoDocument> fetchRelayInfoDocument() {
        return Mono.defer(() -> {
            UriComponents https = UriComponentsBuilder.fromUri(this.relay.getUri())
                    .scheme("https").build();
            return fetchRelayInfoDocument(https.toUri());
        }).onErrorResume(SSLException.class, throwable -> {
            UriComponents http = UriComponentsBuilder.fromUri(this.relay.getUri())
                    .scheme("http").build();
            return fetchRelayInfoDocument(http.toUri());
        });
    }

    @Override
    public Mono<RelayInfoDocument> fetchRelayInfoDocument(URI uri) {
        return Mono.fromCallable(() -> {
            HttpRequest request = HttpRequest.newBuilder().GET()
                    .header("Accept", "application/nostr+json")
                    .uri(uri)
                    .build();

            try (HttpClient client = HttpClient.newHttpClient()) {
                return client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));
            }
        }).map(it -> {
            if (!HttpStatus.valueOf(it.statusCode()).is2xxSuccessful()) {
                throw new RuntimeException(new IOException("Unexpected status code."));
            }

            try {
                return RelayInfoDocument.fromJson(it.body());
            } catch (Exception e) {
                throw new RuntimeException(new IOException("Unexpected data in response body."));
            }
        });
    }

    @Override
    public Mono<Event> fetchEventById(EventId id) {
        return fetchEventsByIds(Collections.singletonList(id)).next();
    }

    @Override
    public Flux<Event> fetchEventsByIds(Collection<EventId> ids) {
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
    public Flux<Event> fetchEventsByAuthors(Collection<XonlyPublicKey> publicKeys) {
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
                .map(it -> JsonReader.fromJson(it.getContent(), Metadata.newBuilder()))
                .next();
    }

    @Override
    public Flux<Event> fetchEvents(ReqRequest request) {
        return fetch(request)
                .filter(it -> Response.KindCase.EVENT.equals(it.getKindCase()))
                .map(Response::getEvent)
                .map(EventResponse::getEvent);
    }

    public Flux<Response> fetch(ReqRequest request) {
        return sendPlain(JsonWriter.toJson(Request.newBuilder()
                .setReq(request)
                .build()))
                .filter(matchingSubscriptionId(request))
                .map(verifyEventSignature())
                .takeUntil(it -> Response.KindCase.EOSE.equals(it.getKindCase()) || Response.KindCase.CLOSED.equals(it.getKindCase()));
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
                            Response response = JsonReader.fromJson(message.getPayload(), Response.newBuilder());
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

                TextMessage message = new TextMessage(JsonWriter.toJson(Request.newBuilder()
                        .setCount(request)
                        .build()));

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
    public Flux<OkResponse> send(Collection<Event> eventList) {
        Set<Event> events = Set.copyOf(eventList);

        Set<ByteString> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toSet());

        int eventCount = eventIds.size();
        ConcurrentHashMap<ByteString, OkResponse> received = new ConcurrentHashMap<>();

        return sendAndCollect(eventList)
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .filter(ok -> eventIds.contains(ok.getEventId()))
                .doOnNext(ok -> {
                    received.put(ok.getEventId(), ok);
                })
                .takeUntil(ok -> received.size() >= eventCount);
    }

    @Override
    public Flux<Response> sendAndCollect(Collection<Event> events) {
        return sendPlain(toJsonMessages(events));
    }

    @Override
    public Flux<Response> sendPlain(Collection<String> messages) {
        AtomicReference<WebSocketSession> sessionRef = new AtomicReference<>();

        return Flux.<Response>create(sink -> {
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
                            Response response = JsonReader.fromJson(message.getPayload(), Response.newBuilder());
                            sink.next(response);
                        } catch (Exception e) {
                            log.warn("Error in handleTextMessage while handling '{}': {}", message, e.getMessage());
                            sink.error(e);
                        }
                    }
                }, headers, relay.getUri()).get());

                for (String message : messages) {
                    TextMessage textMessage = new TextMessage(message);
                    log.debug("Sending message: {}", textMessage.getPayload());
                    sessionRef.get().sendMessage(textMessage);
                }
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

    private static List<String> toJsonMessages(Collection<Event> events) {
        return events.stream()
                .map(it -> Request.newBuilder()
                        .setEvent(EventRequest.newBuilder()
                                .setEvent(it)
                                .build())
                        .build())
                .map(JsonWriter::toJson)
                .toList();
    }

    private static Predicate<Response> matchingSubscriptionId(ReqRequest request) {
        return it -> {
            if (Response.KindCase.EVENT.equals(it.getKindCase())) {
                EventResponse eventResponse = it.getEvent();
                if (!eventResponse.getSubscriptionId().equals(request.getId())) {
                    log.warn("{} on unexpected subscription received. Ignoring.", it.getKindCase());
                    return false;
                }
            } else if (Response.KindCase.EOSE.equals(it.getKindCase())) {
                EoseResponse eoseResponse = it.getEose();
                if (!eoseResponse.getSubscriptionId().equals(request.getId())) {
                    log.warn("{} on unexpected subscription received. Ignoring.", it.getKindCase());
                    return false;
                }
            } else if (Response.KindCase.CLOSED.equals(it.getKindCase())) {
                ClosedResponse closedResponse = it.getClosed();
                if (!closedResponse.getSubscriptionId().equals(request.getId())) {
                    log.warn("{} on unexpected subscription received. Ignoring.", it.getKindCase());
                    return false;
                }
            } else if (Response.KindCase.COUNT.equals(it.getKindCase())) {
                CountResponse countResponse = it.getCount();
                if (!countResponse.getSubscriptionId().equals(request.getId())) {
                    log.warn("{} on unexpected subscription received. Ignoring.", it.getKindCase());
                    return false;
                }
            }

            return true;
        };
    }

    private static Function<Response, Response> verifyEventSignature() {
        return it -> {
            if (Response.KindCase.EVENT.equals(it.getKindCase())) {
                EventResponse eventResponse = it.getEvent();
                if (!MoreEvents.hasValidSignature(eventResponse.getEvent())) {
                    throw new IllegalArgumentException("Invalid event data");
                }
            }
            return it;
        };
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
