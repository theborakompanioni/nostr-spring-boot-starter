package org.tbk.nostr.nips;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/40.md">NIP-40</a>.
 */
public final class Nip40 {
    private static final String EXPIRATION_TAG_NAME = "expiration";

    private Nip40() {
        throw new UnsupportedOperationException();
    }

    public static TagValue expiration(Duration duration) {
        return expiration(Instant.now().plusNanos(duration.toNanos()));
    }

    public static TagValue expiration(Instant instant) {
        return MoreTags.named(EXPIRATION_TAG_NAME, String.valueOf(instant.getEpochSecond()));
    }

    public static Optional<Instant> getExpiration(Event event) {
        return event.getTagsList().stream()
                .filter(it -> EXPIRATION_TAG_NAME.equals(it.getName()))
                .filter(it -> it.getValuesCount() > 0)
                .map(it -> it.getValues(0))
                .flatMap(it -> {
                    try {
                        return Stream.of(Long.parseLong(it));
                    } catch (Exception e) {
                        return Stream.empty();
                    }
                })
                .filter(it -> it >= 0)
                .min(Comparator.comparingLong(value -> value))
                .map(Instant::ofEpochSecond);
    }
}
