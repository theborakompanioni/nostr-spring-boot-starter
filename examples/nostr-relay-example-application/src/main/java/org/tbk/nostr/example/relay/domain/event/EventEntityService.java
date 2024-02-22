package org.tbk.nostr.example.relay.domain.event;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public interface EventEntityService {

    EventEntity createEvent(Event event);

    default Optional<EventEntity> findById(EventId eventId) {
        return findById(EventEntity.EventEntityId.of(eventId.toHex()));
    }

    Optional<EventEntity> findById(EventEntity.EventEntityId eventId);

    List<EventEntity> findAll(Collection<Filter> filters);

    Page<EventEntity> findAll(Specification<EventEntity> specs, Pageable page);

    boolean exists(Specification<EventEntity> specs);

    default List<EventId> markDeleted(XonlyPublicKey author, EventId deletableEventId) {
        return markDeletedByEventIds(author, List.of(deletableEventId));
    }

    default List<EventId> markDeleted(XonlyPublicKey author, EventUri deletableEventUri) {
        return markDeletedByEventUris(author, List.of(deletableEventUri));
    }

    default List<EventId> markDeletedByEventIds(XonlyPublicKey author, Collection<EventId> deletableEventIds) {
        return markDeleted(author, Specification.anyOf(deletableEventIds.stream()
                .map(EventEntitySpecifications::hasId)
                .collect(Collectors.toList())));
    }

    default List<EventId> markDeletedByEventUris(XonlyPublicKey author, Collection<EventUri> deletableEventUris) {
        return markDeleted(author, Specification.anyOf(deletableEventUris.stream()
                .map(EventEntitySpecifications::matches)
                .collect(Collectors.toList())));
    }

    List<EventId> markDeleted(XonlyPublicKey author, Specification<EventEntity> specs);

    EventEntity markExpiresAt(EventId eventId, Instant expiresAt);
}
