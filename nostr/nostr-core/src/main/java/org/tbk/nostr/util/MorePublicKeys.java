package org.tbk.nostr.util;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.proto.Event;

import java.util.HexFormat;

public final class MorePublicKeys {

    private MorePublicKeys() {
        throw new UnsupportedOperationException();
    }

    public static boolean isValidPublicKeyString(String value) {
        if (value.length() != 64) {
            return false;
        }
        try {
            return isValidPublicKey(HexFormat.of().parseHex(value));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidPublicKey(byte[] raw) {
        if (raw.length != 32) {
            return false;
        }
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

    public static XonlyPublicKey fromHex(String hex) throws IllegalArgumentException {
        return fromBytes(HexFormat.of().parseHex(hex));
    }

}
