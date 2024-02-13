package org.tbk.nostr.relay.example.nostr.extension.nip9;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.proto.Event;
import reactor.core.publisher.Mono;

import java.util.Collection;


public interface Nip9Support {

    Mono<Void> markDeletedByEventIds(XonlyPublicKey author, Collection<EventId> deletableEventIds);

    Mono<Void> markDeletedByEventUris(XonlyPublicKey author, Collection<EventUri> deletableEventUris);

    Mono<Boolean> hasDeletionEvent(XonlyPublicKey author, Event event);
}
