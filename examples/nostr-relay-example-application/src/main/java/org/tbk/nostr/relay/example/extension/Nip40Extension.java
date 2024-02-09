package org.tbk.nostr.relay.example.extension;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.nips.Nip40;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityEvents;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;

@Slf4j
@Component
@RequiredArgsConstructor
public class Nip40Extension {

    @NonNull
    private final EventEntityService eventEntityService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(EventEntityEvents.CreatedEvent created) {
        EventEntity entity = eventEntityService.findById(created.eventId())
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Could not find EventEntity from CreatedEvent"));

        Event event = entity.toNostrEvent();

        Nip40.getExpiration(event).ifPresent(expiresAt -> {
            eventEntityService.markExpiresAt(entity.getId().toEventId(), expiresAt);
        });
    }

    void afterEventCreated(EventEntity entity) {

    }
}
