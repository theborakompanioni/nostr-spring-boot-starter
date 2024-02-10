package org.tbk.nostr.nips;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/13.md">NIP-13</a>.
 */
public final class Nip13 {
    private Nip13() {
        throw new UnsupportedOperationException();
    }

    public static Event.Builder mineEvent(Event.Builder prototype, int targetDifficulty) {
        int prototypeTagCount = prototype.getTagsCount();
        long i = 0;

        TagValue.Builder nonceBuilder = MoreTags.named("nonce", String.valueOf(i), String.valueOf(targetDifficulty)).toBuilder();
        Event.Builder builder = Event.newBuilder(prototype.buildPartial())
                .addTags(nonceBuilder);

        long currentEpoch = System.currentTimeMillis() / 1_000;
        long currentDifficulty = 0;
        while (currentDifficulty < targetDifficulty) {
            long runEpoch = System.currentTimeMillis() / 1_000;
            if (runEpoch > currentEpoch) {
                currentEpoch = runEpoch;
                builder.setCreatedAt(Instant.now().toEpochMilli());
                i = 0;
            }

            nonceBuilder.setValues(0, String.valueOf(i));
            builder.setTags(prototypeTagCount, nonceBuilder.build());
            byte[] bytes = MoreEvents.eventId(builder);

            currentDifficulty = calculateDifficulty(bytes);
            i++;
        }

        return MoreEvents.withEventId(builder);
    }

    public static long calculateDifficulty(EventOrBuilder event) {
        return calculateDifficulty(MoreEvents.eventId(event));
    }

    public static long calculateDifficulty(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(it -> toBitSet(bytes[it]))
                .flatMap(bitSet -> IntStream.range(0, 8)
                        .map(it -> 8 - it - 1)
                        .mapToObj(bitSet::get))
                .takeWhile(it -> !it)
                .count();
    }

    private static BitSet toBitSet(byte... values) {
        return BitSet.valueOf(values);
    }
}
