package org.tbk.nostr.relay.example.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.proto.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface Nip1Support {

    Flux<Event> findAllAfterCreatedAt(XonlyPublicKey author, int kind, Instant createdAt);


    Mono<Void> markDeletedBeforeCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);
}
