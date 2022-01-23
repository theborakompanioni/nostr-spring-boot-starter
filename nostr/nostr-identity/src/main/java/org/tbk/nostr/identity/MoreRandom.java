package org.tbk.nostr.identity;

import java.security.SecureRandom;

final class MoreRandom {
    private static final SecureRandom RANDOM = new SecureRandom();

    private MoreRandom() {
        throw new UnsupportedOperationException();
    }

    static byte[] randomByteArray(int len) {
        byte[] bytes = new byte[len];
        RANDOM.nextBytes(bytes);
        return bytes;
    }
}
