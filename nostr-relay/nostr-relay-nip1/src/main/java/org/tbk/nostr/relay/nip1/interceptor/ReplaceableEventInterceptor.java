package org.tbk.nostr.relay.nip1.interceptor;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.nip1.Nip1Support;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
public class ReplaceableEventInterceptor implements RequestHandlerInterceptor {

    @NonNull
    private final Nip1Support support;

    @Override
    public boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            return handleEvent(context, request.getEvent().getEvent());
        }
        return true;
    }

    private boolean handleEvent(NostrRequestContext context, Event event) {
        if (Nip1.isReplaceableEvent(event) || Nip1.isAddressableEvent(event)) {
            XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);

            Instant eventCreatedAt = Instant.ofEpochSecond(event.getCreatedAt());

            List<Event> newerExistingEvents = findExistingReplaceableEventsWithCreatedAfterOrEqual(event, publicKey, eventCreatedAt);

            Optional<ReplaceableError> replaceableErrorOrEmpty = checkReplaceableEventRules(event, newerExistingEvents);

            if (replaceableErrorOrEmpty.isPresent()) {
                context.add(Response.newBuilder()
                        .setOk(OkResponse.newBuilder()
                                .setEventId(event.getId())
                                .setSuccess(false)
                                .setMessage(replaceableErrorOrEmpty.get().getMessage())
                                .build())
                        .build());
                return false;
            }

            deleteEventsBefore(event, publicKey, eventCreatedAt)
                    .block(Duration.ofSeconds(60));
        }

        return true;
    }

    private Mono<Void> deleteEventsBefore(Event event, XonlyPublicKey publicKey, Instant eventCreatedAt) {
        if (Nip1.isReplaceableEvent(event)) {
            return support.deleteAllBeforeCreatedAtInclusive(publicKey, event.getKind(), eventCreatedAt);
        } else if (Nip1.isAddressableEvent(event)) {
            IndexedTag identifier = IndexedTag.d;

            TagValue identifierTag = MoreTags.findByNameSingle(event, identifier)
                    .orElseThrow(() -> new IllegalStateException("Error while replacing events: Missing or conflicting '%s' tag.".formatted(identifier.name())));

            String firstIdentifierValueOrNull = identifierTag.getValuesCount() == 0 ? null : identifierTag.getValues(0);

            return support.deleteAllBeforeCreatedAtInclusiveWithTag(publicKey, event.getKind(), eventCreatedAt, identifier, firstIdentifierValueOrNull);
        } else {
            throw new IllegalStateException("Only pass replaceable events to this function");
        }
    }

    private List<Event> findExistingReplaceableEventsWithCreatedAfterOrEqual(Event event, XonlyPublicKey publicKey, Instant eventCreatedAt) {
        if (Nip1.isReplaceableEvent(event)) {
            return support
                    .findAllAfterCreatedAtInclusive(publicKey, event.getKind(), eventCreatedAt)
                    .collectList()
                    .blockOptional(Duration.ofSeconds(60))
                    .orElseThrow(() -> new IllegalStateException("Error while replacing events: Fetch phase."));
        } else if (Nip1.isAddressableEvent(event)) {
            IndexedTag identifier = IndexedTag.d;

            TagValue identifierTag = MoreTags.findByNameSingle(event, identifier)
                    .orElseThrow(() -> new IllegalStateException("Error while replacing events: Missing or conflicting '%s' tag.".formatted(identifier.name())));

            String firstIdentifierValueOrNull = identifierTag.getValuesCount() == 0 ? null : identifierTag.getValues(0);

            return support
                    .findAllAfterCreatedAtInclusiveWithTag(publicKey, event.getKind(), eventCreatedAt, identifier, firstIdentifierValueOrNull)
                    .collectList()
                    .blockOptional(Duration.ofSeconds(60))
                    .orElseThrow(() -> new IllegalStateException("Error while replacing events: Fetch phase."));
        } else {
            throw new IllegalStateException("Only pass replaceable events to this function");
        }
    }

    @Getter
    @RequiredArgsConstructor
    enum ReplaceableError {
        NEWER_VERSION("error: A newer version of this replaceable event already exists."),
        LOWER_ID("error: A version of this replaceable event with same timestamp and lower id already exists.");

        @NonNull
        private final String message;
    }

    private Optional<ReplaceableError> checkReplaceableEventRules(Event event, List<Event> existingEvents) {
        if (!existingEvents.isEmpty()) {
            if (existingEvents.size() > 1) {
                log.warn("Found {} replaceable events, when there should only be at most one such event: {}",
                        existingEvents.size(),
                        event);
            }

            List<Event> existingEventsWithSameCreatedAt = existingEvents.stream()
                    .filter(it -> it.getCreatedAt() == event.getCreatedAt())
                    .toList();

            if (existingEvents.size() != existingEventsWithSameCreatedAt.size()) {
                return Optional.of(ReplaceableError.NEWER_VERSION);
            }

            // From NIP-01:
            // "In case of replaceable events with the same timestamp, the event with the lowest id
            // (first in lexical order) should be retained, and the other discarded."
            List<EventId> eventIds = existingEventsWithSameCreatedAt.stream()
                    .map(EventId::of)
                    .toList();

            EventId incomingEventId = EventId.of(event);
            Optional<EventId> lowestEventId = MoreEvents.findLowestEventId(Stream.concat(eventIds.stream(), Stream.of(incomingEventId)).toList());
            if (lowestEventId.isPresent() && !lowestEventId.get().equals(incomingEventId)) {
                return Optional.of(ReplaceableError.LOWER_ID);
            }
        }
        return Optional.empty();
    }
}
