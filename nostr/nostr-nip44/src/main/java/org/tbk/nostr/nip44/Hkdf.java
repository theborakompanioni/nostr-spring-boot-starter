package org.tbk.nostr.nip44;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;

import java.util.Arrays;

final class Hkdf {

    private static final int SHA256_OUT_BYTES = 32;
    private static final int MAX_LENGTH = 255 * SHA256_OUT_BYTES;

    private Hkdf() {
        throw new UnsupportedOperationException();
    }

    static byte[] extract(XonlyPublicKey ikm, byte[] salt) {
        return Hmac.hmacSha256(salt, ikm.value.toByteArray());
    }

    static byte[] expand(PrivateKey prk, byte[] info, int length) {
        if (length > MAX_LENGTH) {
            throw new IllegalArgumentException("Invalid length: Expected <= %d, got %d.".formatted(MAX_LENGTH, length));
        }

        int blocks = (int) Math.ceil((double) length / SHA256_OUT_BYTES);

        byte[] okm = new byte[blocks * SHA256_OUT_BYTES];
        byte[] t = new byte[0]; // T(0) = empty string (zero length)
        byte[] counter = new byte[1]; // single byte counter

        for (int i = 0; i < blocks; i++) {
            counter[0] = (byte) (i + 1); // N = counter + 1
            // Concatenate T(N-1) + info + counter
            byte[] combined = new byte[t.length + info.length + counter.length];
            System.arraycopy(t, 0, combined, 0, t.length);
            System.arraycopy(info, 0, combined, t.length, info.length);
            System.arraycopy(counter, 0, combined, t.length + info.length, counter.length);

            t = Hmac.hmacSha256(prk.value.toByteArray(), combined);
            System.arraycopy(t, 0, okm, SHA256_OUT_BYTES * i, SHA256_OUT_BYTES);
        }

        return Arrays.copyOf(okm, length);
    }
}
