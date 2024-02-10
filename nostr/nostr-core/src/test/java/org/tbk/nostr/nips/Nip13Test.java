package org.tbk.nostr.nips;

import com.google.common.base.Stopwatch;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
import java.util.HexFormat;
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
    void itShouldMineEvent0(RepetitionInfo info) {
        int targetDifficulty = 0;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        TagValue nonceTag = MoreTags.filterTagsByName(event, "nonce").getFirst();
        assertThat("it took zero tries", nonceTag.getValues(0), is("0"));
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));
    }

    @RepeatedTest(21)
    void itShouldMineEvent1(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 1;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName(event, "nonce").getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }

    @RepeatedTest(21)
    void itShouldMineEvent8(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 8;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName(event, "nonce").getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);
    }

    @RepeatedTest(16)
    void itShouldMineEventN(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName(event, "nonce").getFirst();
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
    void itShouldMineEventManually(RepetitionInfo info) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int targetDifficulty = 21;

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.calculateDifficulty(event), is(greaterThanOrEqualTo((long) targetDifficulty)));
        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
        assertThat(event.getCreatedAt(), is(both(greaterThanOrEqualTo(Instant.now().minusSeconds(1).getEpochSecond()))
                .and(lessThanOrEqualTo(Instant.now().plusSeconds(1).getEpochSecond()))));

        String bitString = toBitString(event.getId().toByteArray());
        assertThat(bitString, hasLength(256));
        assertThat("it has enough zero bits", bitString, startsWith("0".repeat(targetDifficulty)));

        TagValue nonceTag = MoreTags.filterTagsByName(event, "nonce").getFirst();
        assertThat("it committed to target difficulty", nonceTag.getValues(1), is(Long.toString(targetDifficulty)));

        log.info("difficulty({}): Took {} and {} tries in the last epoch to find {}", targetDifficulty, stopwatch.stop(), nonceTag.getValues(0), bitString);

        log.info("{}", JsonWriter.toJson(Request.newBuilder()
                .setEvent(EventRequest.newBuilder().setEvent(event).build())
                .build()));
    }

    @RepeatedTest(3)
    void itShouldMeetDifficultyTarget0(RepetitionInfo info) {
        int targetDifficulty = 3 + info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content), targetDifficulty).build();

        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
    }

    /**
     * Special case - pass check, if all the nonce tags commit to the same target difficulty!
     */
    @Test
    void itShouldMeetDifficultyTarget1() {
        int targetDifficulty = 1;

        String content = "GM-%d".formatted(targetDifficulty);
        Event event = Nip13.mineEvent(Nip1.createTextNote(testPubkey, content)
                        .addTags(Nip13.nonce(0, targetDifficulty))
                        .addTags(Nip13.nonce(0, targetDifficulty))
                        .addTags(Nip13.nonce(0, targetDifficulty))
                        .addTags(Nip13.nonce(0, targetDifficulty)),
                targetDifficulty).build();

        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(true));
    }

    @Test
    void itShouldNotMeetDifficultyTarget0() {
        Event event = MoreEvents.withEventId(Nip1.createTextNote(testPubkey, "GM")).build();

        assertThat(Nip13.meetsTargetDifficulty(event, 0), is(false));
        assertThat(Nip13.meetsTargetDifficulty(event, 0, false), is(true));
    }

    @RepeatedTest(3)
    void itShouldNotMeetDifficultyTarget1(RepetitionInfo info) {
        int targetDifficulty = 3 + info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event event = MoreEvents.withEventId(Nip1.createTextNote(testPubkey, content)).build();

        assertThat(Nip13.meetsTargetDifficulty(event, targetDifficulty), is(false));
    }

    @RepeatedTest(3)
    void itShouldNotMeetDifficultyTarget2(RepetitionInfo info) {
        int targetDifficulty = 3 + info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event eventWithWrongCommitment0 = MoreEvents.withEventId(Nip1.createTextNote(testPubkey, content))
                .addTags(Nip13.nonce(0, targetDifficulty + 1))
                .build();

        assertThat(Nip13.meetsTargetDifficulty(eventWithWrongCommitment0, targetDifficulty), is(false));

        Event eventWithWrongCommitment1 = MoreEvents.withEventId(Nip1.createTextNote(testPubkey, content))
                .addTags(Nip13.nonce(0, targetDifficulty - 1))
                .build();

        assertThat(Nip13.meetsTargetDifficulty(eventWithWrongCommitment1, targetDifficulty), is(false));
    }

    @RepeatedTest(3)
    void itShouldNotMeetDifficultyTarget3(RepetitionInfo info) {
        int targetDifficulty = 3 + info.getCurrentRepetition();

        String content = "GM-%d-%d".formatted(targetDifficulty, info.getCurrentRepetition());
        Event eventWithMultipleCommitments = MoreEvents.withEventId(Nip1.createTextNote(testPubkey, content))
                .addTags(Nip13.nonce(0, targetDifficulty + 1))
                .addTags(Nip13.nonce(0, targetDifficulty))
                .build();

        assertThat(Nip13.meetsTargetDifficulty(eventWithMultipleCommitments, targetDifficulty), is(false));
    }

    @Test
    void itShouldCalculateDifficulty0EventId() {
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("7f0fdf9021cbde815007340d603b43e61ddecac58a337165974b74eb843ba4bc")), is(1L));
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("078c03419eed66e9a5ce7c8f7ac6e83e2952b600755781ac75da9950259b8bfa")), is(5L));
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358")), is(21L));
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("00000000000009fb3d037ce2fcb5bd2a84914f98e6f13352495bbc5b708d162c")), is(52L));
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("0000000000000000000000000000000c25e1493f5a8465fd71802790e2a58a32")), is(124L));
        assertThat(Nip13.calculateDifficulty(EventId.fromHex("00000000000000000000000000000000000000000000002a6fbf90a18e816d60")), is(186L));
    }

    @RepeatedTest(128)
    void itShouldCalculateDifficulty1VariableLength(RepetitionInfo info) {
        int i = info.getCurrentRepetition();
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("00".repeat(i))), is(i * 8L));
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0f".repeat(i))), is(4L));
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("ff".repeat(i))), is(0L));
    }

    @Test
    void itShouldCalculateDifficulty2Static() {
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("00")), is(8L)); // 0000 0000
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("01")), is(7L)); // 0000 0001
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("02")), is(6L)); // 0000 0010
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("03")), is(6L)); // 0000 0011
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("04")), is(5L)); // 0000 0100
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("05")), is(5L)); // 0000 0101
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("06")), is(5L)); // 0000 0110
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("07")), is(5L)); // 0000 0111
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("08")), is(4L)); // 0000 1000
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("09")), is(4L)); // 0000 1001
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0a")), is(4L)); // 0000 1010
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0b")), is(4L)); // 0000 1011
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0c")), is(4L)); // 0000 1100
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0d")), is(4L)); // 0000 1101
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0e")), is(4L)); // 0000 1110
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("0f")), is(4L)); // 0000 1111
        // ...
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("1f")), is(3L)); // 0001
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("2f")), is(2L)); // 0010
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("3f")), is(2L)); // 0011
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("4f")), is(1L)); // 0100
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("5f")), is(1L)); // 0101
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("6f")), is(1L)); // 0110
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("7f")), is(1L)); // 0111
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("8f")), is(0L)); // 1000
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("9f")), is(0L)); // 1001
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("af")), is(0L)); // 1010
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("bf")), is(0L)); // 1011
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("cf")), is(0L)); // 1100
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("df")), is(0L)); // 1101
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("ef")), is(0L)); // 1110
        assertThat(Nip13.calculateDifficulty(HexFormat.of().parseHex("ff")), is(0L)); // 1111
    }
}
