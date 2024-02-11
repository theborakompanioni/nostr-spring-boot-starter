package org.tbk.nostr.relay.example.extension.nip40;

import org.tbk.nostr.base.EventId;

import java.time.Instant;


public interface Nip40Support {

    void markExpiresAt(EventId eventId, Instant expiresAt);
}
