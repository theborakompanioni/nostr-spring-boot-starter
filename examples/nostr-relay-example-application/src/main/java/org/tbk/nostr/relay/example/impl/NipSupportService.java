package org.tbk.nostr.relay.example.impl;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.domain.event.EventEntity;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.domain.event.EventEntitySpecifications;
import org.tbk.nostr.relay.example.extension.nip1.Nip1Support;
import org.tbk.nostr.relay.example.extension.nip40.Nip40Support;
import org.tbk.nostr.relay.example.extension.nip9.Nip9Support;
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
    public Flux<Event> findAllAfterCreatedAt(XonlyPublicKey author, int kind, Instant createdAt) {
        return Flux.defer(() -> {
            PageRequest pageRequest = PageRequest.of(0, Integer.MAX_VALUE);

            Specification<EventEntity> specs = EventEntitySpecifications.hasPubkey(author)
                    .and(EventEntitySpecifications.hasKind(kind))
                    .and(EventEntitySpecifications.isCreatedAfterInclusive(createdAt))
                    .and(EventEntitySpecifications.isNotDeleted())
                    .and(EventEntitySpecifications.isNotExpired());

            return Flux.fromIterable(eventEntityService.findAll(specs, pageRequest).toList());
        }).map(EventEntity::toNostrEvent);
    }

    @Override
    public Mono<Void> markDeletedBeforeCreatedAtInclusive(XonlyPublicKey publicKey, int kind, Instant createdAt) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeleted(publicKey,
                    EventEntitySpecifications.hasPubkey(publicKey)
                            .and(EventEntitySpecifications.hasKind(kind))
                            .and(EventEntitySpecifications.isCreatedBeforeInclusive(createdAt))
                            .and(EventEntitySpecifications.isNotDeleted())
            );
        }).subscribeOn(asyncScheduler);
    }

    @Override
    public Mono<Void> markExpiresAt(EventId eventId, Instant expiresAt) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markExpiresAt(eventId, expiresAt);
        }).subscribeOn(asyncScheduler);
    }

    @Override
    public Mono<Void> markDeleted(XonlyPublicKey author, Collection<EventId> deletableEventIds) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeleted(author, deletableEventIds);
        }).subscribeOn(asyncScheduler);
    }

    @Override
    public Mono<Boolean> hasDeletionEvent(XonlyPublicKey author, Event event) {
        return Mono.fromCallable(() -> {
            EventId eventId = EventId.of(event.getId().toByteArray());
            return eventEntityService.exists(EventEntitySpecifications.hasTagWithFirstValue('e', eventId.toHex())
                    .and(EventEntitySpecifications.hasPubkey(author))
                    .and(EventEntitySpecifications.hasKind(Nip9.kind())));
        }).subscribeOn(asyncScheduler);
    }
}
