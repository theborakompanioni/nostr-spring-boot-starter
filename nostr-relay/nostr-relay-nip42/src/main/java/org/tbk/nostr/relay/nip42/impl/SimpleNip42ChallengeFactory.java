package org.tbk.nostr.relay.nip42.impl;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HexFormat;

final class SimpleNip42ChallengeFactory {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static byte[] random() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return bytes;
    }

    public byte[] create() {
        return random();
    }
}
