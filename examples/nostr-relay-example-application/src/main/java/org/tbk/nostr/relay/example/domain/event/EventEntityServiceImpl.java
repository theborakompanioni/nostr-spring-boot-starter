package org.tbk.nostr.relay.example.domain.event;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.proto.Event;

import java.time.Instant;

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