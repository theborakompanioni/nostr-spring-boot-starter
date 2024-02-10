package org.tbk.nostr.nips;

import com.google.common.base.Stopwatch;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Slf4j
class Nip13Test {
    private static final XonlyPublicKey testPubkey = SimpleSigner.random().getPublicKey();

    private static String toBitString(byte[] bytes) {
        return IntStream.range(0, bytes.length)
                .mapToObj(it -> bytes[it])
                .map(it -> String.format("%8s", Integer.toBinaryString(it & 0xFF)).replace(' ', '0'))
                .collect(Collectors.joining());
    }

    @RepeatedTest(21)
    void mineEvent0(RepetitionInfo info) {
        int targetDifficulty = 0;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it took zero tries", nonceTag.getValues(0), is("0"));
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));
    }

    @RepeatedTest(21)
    void mineEvent1(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 1;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat(bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }

    @RepeatedTest(21)
    void mineEvent8(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 8;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat(bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }

    @RepeatedTest(16)
    void mineEventN(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat(bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }
}
