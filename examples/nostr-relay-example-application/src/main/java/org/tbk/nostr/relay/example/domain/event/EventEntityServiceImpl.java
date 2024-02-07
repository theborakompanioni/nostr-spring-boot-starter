package org.tbk.nostr.relay.example.domain.event;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EventEntityServiceImpl implements EventEntityService {

    @NonNull
    private final EventEntities events;

    @Override
    public EventEntity createEvent(Event event) {
        return events.save(new EventEntity(event));
    }

    @Override
    public Flux<EventEntity> find(List<Filter> filters) {
        List<Specification<EventEntity>> filterSpecifications = filters.stream()
                .map(EventEntityServiceImpl::toSpecification)
                .toList();

        Specification<EventEntity> specification = Specification.allOf(
                Specification.anyOf(filterSpecifications),
                EventEntitySpecifications.isNotDeleted(),
                EventEntitySpecifications.isNotExpired()
        );

        return Flux.fromIterable(events.findAll(specification));
    }

    private static Specification<EventEntity> toSpecification(Filter filter) {
        Specification<EventEntity> idsSpecification = Specification.anyOf(filter.getIdsList().stream()
                .map(it -> EventId.of(it.toByteArray()))
                .map(EventEntitySpecifications::hasId)
                .toList());

        Specification<EventEntity> authorsSpecification = Specification.anyOf(filter.getAuthorsList().stream()
                .map(it -> new XonlyPublicKey(new ByteVector32(it.toByteArray())))
                .map(EventEntitySpecifications::hasPubkey)
                .toList());

        Specification<EventEntity> kindsSpecification = Specification.anyOf(filter.getKindsList().stream()
                .map(EventEntitySpecifications::hasKind)
                .toList());

        return Specification.allOf(
                idsSpecification,
                authorsSpecification,
                kindsSpecification
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(EventEntityEvents.CreatedEvent created) {
        EventEntity entity = events.findById(created.eventId())
                .orElseThrow(() -> new IllegalStateException("Could not find EventEntity from CreatedEvent"));

        log.trace("Successfully saved event {}", entity.getId().getId());

        if (entity.isExpired(Instant.now())) {
            events.save(entity.markDeleted());
        }
    }

    @Async
    void on(EventEntityEvents.MarkDeletedEvent markDeleted) {
        log.debug("Successfully marked event as deleted: {} ", markDeleted.eventId().getId());
    }
}
