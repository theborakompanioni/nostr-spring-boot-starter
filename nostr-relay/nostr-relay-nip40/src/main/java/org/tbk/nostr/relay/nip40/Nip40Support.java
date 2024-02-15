package org.tbk.nostr.relay.nip40;

import org.tbk.nostr.base.EventId;
import reactor.core.publisher.Mono;

import java.time.Instant;


public interface Nip40Support {

    Mono<Void> markExpiresAt(EventId eventId, Instant expiresAt);
}
