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

    public static long calculateDifficulty(EventOrBuilder event) {
        return calculateDifficulty(MoreEvents.eventId(event));
    }

    public static long calculateDifficulty(byte[] bytes) {
        return countLeadingZeroes(bytes);
    }

    public static Event.Builder mineEvent(Event.Builder prototype, int targetDifficulty) {
        int prototypeTagCount = prototype.getTagsCount();
        long nonce = 0;

        TagValue.Builder nonceTagBuilder = MoreTags.named("nonce", Long.toString(nonce), Long.toString(targetDifficulty)).toBuilder();
        Event.Builder builder = Event.newBuilder(prototype.buildPartial()).addTags(nonceTagBuilder);

        long currentEpoch = System.currentTimeMillis() / 1_000;
        long currentDifficulty = 0;
        while (currentDifficulty < targetDifficulty) {
            long runEpoch = System.currentTimeMillis() / 1_000;
            if (runEpoch > currentEpoch) {
                currentEpoch = runEpoch;
                builder.setCreatedAt(Instant.now().toEpochMilli());
                nonce = 0;
            }

            // update "nonce" tag
            builder.setTags(prototypeTagCount, nonceTagBuilder.setValues(0, Long.toString(nonce)));

            currentDifficulty = countLeadingZeroes(MoreEvents.eventId(builder));
            nonce++;
        }

        return MoreEvents.withEventId(builder);
    }

    private static long countLeadingZeroes(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(it -> toBitSet(bytes[it]))
                .flatMap(bitSet -> IntStream.range(0, 8).mapToObj(it -> bitSet.get(7 - it)))
                .takeWhile(it -> !it)
                .count();
    }

    private static BitSet toBitSet(byte... values) {
        return BitSet.valueOf(values);
    }
}
