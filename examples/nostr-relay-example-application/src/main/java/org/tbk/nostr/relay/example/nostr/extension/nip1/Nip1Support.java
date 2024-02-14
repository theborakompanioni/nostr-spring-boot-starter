package org.tbk.nostr.relay.example.nostr.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;

public interface Nip1Support {
    Flux<Event> findAll(Collection<Filter> filters);

    Flux<Event> findAllAfterCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);

    Flux<Event> findAllAfterCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, @Nullable String firstTagValue);

    Mono<Void> markDeletedBeforeCreatedAtInclusive(XonlyPublicKey author, int kind, Instant createdAt);

    Mono<Void> markDeletedBeforeCreatedAtInclusiveWithTag(XonlyPublicKey author, int kind, Instant createdAt, IndexedTag tagName, @Nullable String firstTagValue);

}
