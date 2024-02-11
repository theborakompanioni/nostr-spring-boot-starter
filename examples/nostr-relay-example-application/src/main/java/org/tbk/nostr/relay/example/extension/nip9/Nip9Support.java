package org.tbk.nostr.relay.example.extension.nip9;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import reactor.core.publisher.Mono;

import java.util.Collection;


public interface Nip9Support {

    Mono<Void> markDeleted(Collection<EventId> deletableEventIds, XonlyPublicKey author);

    Mono<Boolean> hasDeletionEvent(Event event, XonlyPublicKey author);
}
