package org.tbk.nostr.util;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.proto.Event;

public final class MorePublicKeys {

    private MorePublicKeys() {
        throw new UnsupportedOperationException();
    }


    public static boolean isValidPublicKey(byte[] raw) {
        try {
            XonlyPublicKey publicKey = fromBytes(raw);
            return publicKey.getPublicKey().isValid();
        } catch (Exception e) {
            return false;
        }
    }

    public static XonlyPublicKey fromEvent(Event event) throws IllegalArgumentException {
        return fromBytes(event.getPubkey().toByteArray());
    }

    public static XonlyPublicKey fromBytes(byte[] raw) throws IllegalArgumentException {
        return new XonlyPublicKey(new ByteVector32(raw));
    }

}
