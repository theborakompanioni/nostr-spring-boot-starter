package org.tbk.nostr.relay.example.extension.nip40;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.nips.Nip40;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityEvents;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;

@Slf4j
@RequiredArgsConstructor
class Nip40EventEntityPostProcessor {

    @NonNull
    private final EventEntityService eventEntityService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(EventEntityEvents.CreatedEvent created) {
        EventEntity entity = eventEntityService.findById(created.eventId())
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Could not find EventEntity from CreatedEvent"));

        Nip40.getExpiration(entity.toNostrEvent()).ifPresent(expiresAt -> {
            eventEntityService.markExpiresAt(entity.getId().toEventId(), expiresAt);
        });
    }
}
