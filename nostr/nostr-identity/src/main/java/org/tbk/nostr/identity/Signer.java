package org.tbk.nostr.identity;


import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.proto.Event;

public interface Signer {
    XonlyPublicKey getPublicKey();

    Event.Builder sign(Event.Builder builder);

}
