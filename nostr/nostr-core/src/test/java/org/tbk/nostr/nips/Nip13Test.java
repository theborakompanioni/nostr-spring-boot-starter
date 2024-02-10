package org.tbk.nostr.nips;

import com.google.common.base.Stopwatch;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
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
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

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
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

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
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

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
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }

    /**
     * Some benchmarks on a "reasonable consumer cpu" (in 2023):
     * - difficulty(19): 594.0 ms, 1.049 s, 1.869 s
     * - difficulty(20): 804.6 ms, 1.596 s, 5.459 s
     * - difficulty(21): Took 4.697 s, 5.517 s, 10.11 s
     * - difficulty(22): Took 6.650 s, 7.027 s, 13.35 s
     * - difficulty(23): Took 11.77 s, 25.58 s, 46.87 s
     * - difficulty(24): Took 1.830 min, 2.485 min, 2.841 min
     * - difficulty(25): Took 36.15 s, 43.45 s, 2.962 min
     */
    @RepeatedTest(3)
    @Disabled("Enable on demand - should not execute on every test suite run")
    void mineEventManually(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 21;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName("nonce", event).getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);

        log.info("{}", JsonWriter.toJson(Request.newBuilder()
                .setEvent(EventRequest.newBuilder().setEvent(event).build())
                .build()));
    }
}
