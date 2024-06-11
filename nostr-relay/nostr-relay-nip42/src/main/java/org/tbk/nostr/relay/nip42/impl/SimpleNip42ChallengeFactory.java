package org.tbk.nostr.relay.nip42.impl;

import java.security.SecureRandom;
import java.util.HexFormat;

final class SimpleNip42ChallengeFactory {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static String random() {
        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    public String create() {
        return random();
    }
}
