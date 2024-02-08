package org.tbk.nostr.relay.example.extension;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityEvents;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.domain.event.EventEntitySpecifications;
import org.tbk.nostr.util.MoreTags;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class Nip9Extension {

    @NonNull
    private final EventEntityService eventEntityService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener
    void on(EventEntityEvents.CreatedEvent created) {
        EventEntity entity = eventEntityService.findById(created.eventId())
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Could not find EventEntity from CreatedEvent"));

        afterEventCreated(entity);
    }

    void afterEventCreated(EventEntity entity) {
        Event event = entity.toNostrEvent();

        if (Nip9.isDeletionEvent(event)) {
            doOnDeletionEventCreated(entity, event);
        } else {
            onNonDeletionEventCreated(entity);
        }
    }

    private void onNonDeletionEventCreated(EventEntity entity) {
        boolean deletionEventFound = hasDeletionEvent(entity);
        if (deletionEventFound) {
            log.debug("Found existing deletion event for newly created event {}", entity.getId().getId());
            eventEntityService.markDeleted(List.of(entity.asEventId()), entity.asPublicKey())
                    .collectList()
                    .blockOptional()
                    .orElseThrow(() -> new IllegalStateException("Could not delete all events"));
        }
    }

    private boolean hasDeletionEvent(EventEntity entity) {
        return eventEntityService.exists(EventEntitySpecifications.hasTagWithFirstValue('e', entity.getId().getId())
                .and(EventEntitySpecifications.hasPubkey(entity.asPublicKey()))
                .and(EventEntitySpecifications.hasKind(Nip9.kind())));
    }

    private void doOnDeletionEventCreated(EventEntity entity, Event event) {
        List<TagValue> eTags = MoreTags.filterTagsByName("e", event);
        // TODO: `a` tags ()

        Set<EventId> deletableEventIds = eTags.stream()
                .map(it -> it.getValues(0))
                .map(EventId::fromHex)
                .collect(Collectors.toSet());

        List<EventId> deletedEventIds = eventEntityService.markDeleted(deletableEventIds, entity.asPublicKey())
                .collectList()
                .blockOptional()
                .orElseThrow(() -> new IllegalStateException("Could not delete all events"));

        log.debug("Marked {} events as deleted based on deletion event {}.", deletedEventIds.size(), entity.getId().getId());
    }
}
