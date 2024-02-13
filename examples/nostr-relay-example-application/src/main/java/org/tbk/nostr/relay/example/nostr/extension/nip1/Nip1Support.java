package org.tbk.nostr.relay.example.nostr.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.proto.Event;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.time.Instant;

public interface Nip1Support {

    Flux<Event> findAllAfterCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);


    Flux<Event> findAllAfterCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, @Nullable String firstTagValue);


    Mono<Void> markDeletedBeforeCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);

    Mono<Void> markDeletedBeforeCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, @Nullable String firstTagValue);

}
