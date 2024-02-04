package org.tbk.nostr.base.util;


import org.tbk.nostr.base.SubscriptionId;

import java.security.SecureRandom;
import java.util.stream.Collectors;


public final class MoreSubscriptionIds {

    private MoreSubscriptionIds() {
        throw new UnsupportedOperationException();
    }

    private static final SecureRandom RANDOM = new SecureRandom();

    public static SubscriptionId random() {
        return random(32);
    }

    public static SubscriptionId random(int minLength) {
        return random(minLength, 64);
    }

    public static SubscriptionId random(int minLength, int maxLength) {
        return SubscriptionId.of(RANDOM.ints(randomLength(minLength, maxLength), 0, 15 + 1).boxed()
                .map(Integer::toHexString)
                .collect(Collectors.joining()));
    }

    private static int randomLength(int minLength, int maxLength) {
        if (minLength < 1) {
            throw new IllegalArgumentException("minLength must be greater than or equal to 1");
        }
        if (maxLength > 64) {
            throw new IllegalArgumentException("maxLength must be lower than or equal to 64");
        }
        if (minLength > maxLength) {
            throw new IllegalArgumentException("minLength must be lower than or equal to maxLength");
        }
        return RANDOM.nextInt(minLength, maxLength + 1);
    }
}
