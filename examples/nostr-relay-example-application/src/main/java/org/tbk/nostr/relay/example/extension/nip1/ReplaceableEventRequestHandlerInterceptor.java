package org.tbk.nostr.relay.example.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class ReplaceableEventRequestHandlerInterceptor implements NostrRequestHandlerInterceptor {

    @NonNull
    private final Nip1Support support;

    @Override
    public boolean preHandle(WebSocketSession session, Request request) throws IOException {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            return handleEvent(session, request.getEvent().getEvent());
        }
        return true;
    }

    private boolean handleEvent(WebSocketSession session, Event event) throws IOException {
        if (Nip1.isReplaceableEvent(event)) {
            return doOnReplaceableEvent(session, event);
        }
        return true;
    }

    private boolean doOnReplaceableEvent(WebSocketSession session, Event event) throws IOException {
        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);

        // should only be at most "one" event, so we can fetch it instead of using "exists" directly
        Instant eventCreatedAt = Instant.ofEpochSecond(event.getCreatedAt());
        List<Event> existingReplaceableEventsWithCreatedAfterOrEqual = support
                .findAllAfterCreatedAt(publicKey, event.getKind(), eventCreatedAt)
                .collectList()
                .blockOptional(Duration.ofSeconds(60))
                .orElseThrow(() -> new IllegalStateException("Error while replacing events: Fetch phase."));

        if (!existingReplaceableEventsWithCreatedAfterOrEqual.isEmpty()) {
            if (existingReplaceableEventsWithCreatedAfterOrEqual.size() > 1) {
                log.warn("Found {} replaceable events, when there should only be at most one such event: {}",
                        existingReplaceableEventsWithCreatedAfterOrEqual.size(),
                        event);
            }

            List<Event> existingReplaceableEventsWithSameCreatedAt = existingReplaceableEventsWithCreatedAfterOrEqual.stream()
                    .filter(it -> it.getCreatedAt() == event.getCreatedAt())
                    .toList();

            if (existingReplaceableEventsWithCreatedAfterOrEqual.size() != existingReplaceableEventsWithSameCreatedAt.size()) {
                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setOk(OkResponse.newBuilder()
                                .setEventId(event.getId())
                                .setSuccess(false)
                                .setMessage("Error: A newer version of this replaceable event already exists.")
                                .build())
                        .build())));

                return false;
            }

            // From NIP-01:
            // "In case of replaceable events with the same timestamp, the event with the lowest id
            // (first in lexical order) should be retained, and the other discarded."
            List<EventId> eventIds = existingReplaceableEventsWithSameCreatedAt.stream()
                    .map(it -> EventId.of(it.getId().toByteArray()))
                    .toList();

            EventId incomingEventId = EventId.of(event.getId().toByteArray());
            Optional<EventId> lowestEventId = MoreEvents.findLowestEventId(Stream.concat(eventIds.stream(), Stream.of(incomingEventId)).toList());
            if (lowestEventId.isPresent() && !lowestEventId.get().equals(incomingEventId)) {
                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setOk(OkResponse.newBuilder()
                                .setEventId(event.getId())
                                .setSuccess(false)
                                .setMessage("Error: A version of this replaceable event with same timestamp and lower id already exists.")
                                .build())
                        .build())));

                return false;
            }
        }

        support.markDeletedBeforeCreatedAtInclusive(publicKey, event.getKind(), eventCreatedAt)
                .block(Duration.ofSeconds(60));

        return true;
    }
}
