package org.tbk.nostr.relay.example.domain.event;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public interface EventEntityService {

    EventEntity createEvent(Event event);

    default Mono<EventEntity> findById(EventId eventId) {
        return findById(EventEntity.EventEntityId.of(eventId.toHex()));
    }

    Mono<EventEntity> findById(EventEntity.EventEntityId eventId);

    Flux<EventEntity> findAll(Collection<Filter> filters);

    Page<EventEntity> findAll(Specification<EventEntity> specs, Pageable page);

    Flux<EventId> markDeleted(Collection<EventId> deletableEventIds, XonlyPublicKey author);
}
