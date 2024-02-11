package org.tbk.nostr.relay.example.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jmolecules.ddd.annotation.Service;
import org.springframework.scheduling.annotation.Async;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.extension.nip40.Nip40Support;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class NipSupportService implements Nip40Support {

    @NonNull
    private final EventEntityService eventEntityService;

    @Async
    @Override
    public void markExpiresAt(EventId eventId, Instant expiresAt) {
        eventEntityService.markExpiresAt(eventId, expiresAt);
    }
}
