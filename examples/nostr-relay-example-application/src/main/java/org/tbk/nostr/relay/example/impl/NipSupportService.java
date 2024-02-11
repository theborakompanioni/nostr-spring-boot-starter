package org.tbk.nostr.relay.example.impl;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.domain.event.EventEntitySpecifications;
import org.tbk.nostr.relay.example.extension.nip40.Nip40Support;
import org.tbk.nostr.relay.example.extension.nip9.Nip9Support;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

public class NipSupportService implements Nip9Support, Nip40Support {

    private final EventEntityService eventEntityService;

    private final Scheduler asyncScheduler;

    public NipSupportService(EventEntityService eventEntityService, ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor) {
        this.eventEntityService = requireNonNull(eventEntityService);
        this.asyncScheduler = Schedulers.fromExecutor(requireNonNull(asyncThreadPoolTaskExecutor));
    }

    @Override
    public Mono<Void> markExpiresAt(EventId eventId, Instant expiresAt) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markExpiresAt(eventId, expiresAt);
        }).subscribeOn(asyncScheduler);
    }

    @Override
    public Mono<Void> markDeleted(Collection<EventId> deletableEventIds, XonlyPublicKey author) {
        return Mono.<Void>fromRunnable(() -> {
            eventEntityService.markDeleted(deletableEventIds, author);
        }).subscribeOn(asyncScheduler);
    }

    @Override
    public Mono<Boolean> hasDeletionEvent(Event event, XonlyPublicKey author) {
        return Mono.fromCallable(() -> {
            EventId eventId = EventId.of(event.getId().toByteArray());
            return eventEntityService.exists(EventEntitySpecifications.hasTagWithFirstValue('e', eventId.toHex())
                    .and(EventEntitySpecifications.hasPubkey(author))
                    .and(EventEntitySpecifications.hasKind(Nip9.kind())));
        }).subscribeOn(asyncScheduler);
    }
}
