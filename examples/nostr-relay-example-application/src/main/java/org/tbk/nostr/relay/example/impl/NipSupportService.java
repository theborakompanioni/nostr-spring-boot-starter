package org.tbk.nostr.relay.example.impl;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.domain.event.EventEntitySpecifications;
import org.tbk.nostr.relay.example.nostr.extension.nip1.Nip1Support;
import org.tbk.nostr.relay.example.nostr.extension.nip40.Nip40Support;
import org.tbk.nostr.relay.example.nostr.extension.nip9.Nip9Support;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class NipSupportService implements Nip1Support, Nip9Support, Nip40Support {

    private final EventEntityService eventEntityService;

    private final Scheduler asyncScheduler;

    public NipSupportService(EventEntityService eventEntityService, ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor) {
        this.eventEntityService = requireNonNull(eventEntityService);
        this.asyncScheduler = Schedulers.fromExecutor(requireNonNull(asyncThreadPoolTaskExecutor));
    }

    @Override
    public Flux<Event> findAll(Collection<Filter> filters) {
        return Flux.defer(() -> Flux.fromIterable(eventEntityService.findAll(filters)))
                .map(EventEntity::toNostrEvent);
    }

    @Override
    public Flux<Event> findAllAfterCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt) {
        return Flux.defer(() -> {
            PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

            Specification<EventEntity> specs = allAfterCreatedAtInclusiveSpec(author, kind, createdAt);

            return Flux.fromIterable(eventEntityService.findAll(specs, pageRequest).toList());
        }).map(EventEntity::toNostrEvent);
    }

    @Override
    public Flux<Event> findAllAfterCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, String firstTagValue) {
        return Flux.defer(() -> {
            PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

            Specification<EventEntity> specs = allAfterCreatedAtInclusiveSpec(author, kind, createdAt)
                    .and(EventEntitySpecifications.hasTagWithFirstValue(tagName, firstTagValue));

            return Flux.fromIterable(eventEntityService.findAll(specs, pageRequest).toList());
        }).map(EventEntity::toNostrEvent);
    }

    @Override
    public Mono<Void> deleteAllBeforeCreatedAtInclusive(XonlyPublicKey publicKey, int kind, Instant createdAt) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeleted(publicKey, allBeforeCreatedAtInclusive(publicKey, kind, createdAt));
        });
    }

    @Override
    public Mono<Void> deleteAllBeforeCreatedAtInclusiveWithTag(XonlyPublicKey publicKey, int kind, Instant createdAt, IndexedTag tagName, String firstTagValue) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeleted(publicKey, allBeforeCreatedAtInclusive(publicKey, kind, createdAt)
                    .and(EventEntitySpecifications.hasTagWithFirstValue(tagName, firstTagValue)));
        });
    }

    @Override
    public Mono<Void> markExpiresAt(EventId eventId, Instant expiresAt) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markExpiresAt(eventId, expiresAt);
        });
    }

    @Override
    public Mono<Void> deleteAllByEventIds(XonlyPublicKey author, Collection<EventId> deletableEventIds) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeletedByEventIds(author, deletableEventIds);
        });
    }

    @Override
    public Mono<Void> deleteAllByEventUris(XonlyPublicKey author, Collection<EventUri> deletableEventUris) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeletedByEventUris(author, deletableEventUris);
        });
    }

    @Override
    public Mono<Boolean> hasDeletionEvent(XonlyPublicKey author, Event event) {
        return Mono.fromCallable(() -> {
            EventId eventId = EventId.of(event.getId().toByteArray());
            return eventEntityService.exists(EventEntitySpecifications.hasPubkey(author)
                    .and(EventEntitySpecifications.hasKind(Nip9.kind()))
                    .and(EventEntitySpecifications.hasTagWithFirstValue(IndexedTag.e, eventId.toHex())));
        });
    }

    @NonNull
    private static Specification<EventEntity> allAfterCreatedAtInclusiveSpec(XonlyPublicKey author, int kind, Instant createdAt) {
        return EventEntitySpecifications.hasPubkey(author)
                .and(EventEntitySpecifications.hasKind(kind))
                .and(EventEntitySpecifications.isCreatedAfterInclusive(createdAt))
                .and(EventEntitySpecifications.isNotDeleted())
                .and(EventEntitySpecifications.isNotExpired());
    }

    @NonNull
    private static Specification<EventEntity> allBeforeCreatedAtInclusive(XonlyPublicKey publicKey, int kind, Instant createdAt) {
        return EventEntitySpecifications.hasPubkey(publicKey)
                .and(EventEntitySpecifications.hasKind(kind))
                .and(EventEntitySpecifications.isCreatedBeforeInclusive(createdAt))
                .and(EventEntitySpecifications.isNotDeleted());
    }
}
