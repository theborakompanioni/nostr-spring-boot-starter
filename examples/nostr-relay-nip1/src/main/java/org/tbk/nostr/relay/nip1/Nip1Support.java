package org.tbk.nostr.relay.nip1;

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

    default Mono<Void> deleteAll(XonlyPublicKey author, int kind) {
        return deleteAllBeforeCreatedAtInclusive(author, kind, Instant.MAX);
    }

    Mono<Void> deleteAllBeforeCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);

    Mono<Void> deleteAllBeforeCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, @Nullable String firstTagValue);

}
