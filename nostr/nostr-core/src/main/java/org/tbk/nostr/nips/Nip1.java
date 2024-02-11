package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-1</a>.
 */
public final class Nip1 {
    private static final int REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE = 10_000;
    private static final int REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE = 20_000;
    private static final int EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE = REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE;
    private static final int EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE = 30_000;
    private static final int PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE = EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE;
    private static final int PARAMETERIZED_REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE = 40_000;


    private Nip1() {
        throw new UnsupportedOperationException();
    }

    public static boolean isReplaceableEvent(EventOrBuilder event) {
        return isReplaceableEvent(event.getKind());
    }

    public static boolean isReplaceableEvent(int kind) {
        return kind == 0 ||
               kind == 3 ||
               (REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE <= kind && kind < REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE);
    }

    public static boolean isEphemeralEvent(EventOrBuilder event) {
        return isEphemeralEvent(event.getKind());
    }

    public static boolean isEphemeralEvent(int kind) {
        return EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE <= kind && kind < EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE;
    }

    public static boolean isParameterizedReplaceableEvent(EventOrBuilder event) {
        return isParameterizedReplaceableEvent(event.getKind());
    }

    public static boolean isParameterizedReplaceableEvent(int kind) {
        return PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE <= kind && kind < PARAMETERIZED_REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE;
    }

    public static Event.Builder createMetadata(XonlyPublicKey publicKey, Metadata metadata) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(0)
                .setContent(JsonWriter.toJson(metadata)));
    }

    public static Event.Builder createTextNote(XonlyPublicKey publicKey, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(1)
                .setContent(content));
    }

    public static Event.Builder createReplaceableEvent(XonlyPublicKey publicKey, String content) {
        return createReplaceableEvent(publicKey, content, REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE);
    }

    public static Event.Builder createReplaceableEvent(XonlyPublicKey publicKey, String content, int kind) {
        if (!isReplaceableEvent(kind)) {
            throw new IllegalArgumentException("Given kind is not replaceable. Must be 0, 3, or %d <= n < %d, got: %d".formatted(
                    REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE,
                    REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE,
                    kind
            ));
        }

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .setContent(content));
    }

    public static Event.Builder createEphemeralEvent(XonlyPublicKey publicKey, String content) {
        return createEphemeralEvent(publicKey, content, EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE);
    }

    public static Event.Builder createEphemeralEvent(XonlyPublicKey publicKey, String content, int kind) {
        if (!isEphemeralEvent(kind)) {
            throw new IllegalArgumentException("Given kind is not ephemeral. Must be %d <= n < %d, got: %d".formatted(
                    EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE,
                    EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE,
                    kind
            ));
        }

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .setContent(content));
    }


    public static Event.Builder createParameterizedReplaceableEvent(XonlyPublicKey publicKey, String content, String d) {
        return createParameterizedReplaceableEvent(publicKey, content, PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE, d);
    }

    public static Event.Builder createParameterizedReplaceableEvent(XonlyPublicKey publicKey, String content, int kind, String d) {
        if (!isParameterizedReplaceableEvent(kind)) {
            throw new IllegalArgumentException("Given kind is not parameterized replaceable. Must be %d <= n < %d, got: %d".formatted(
                    PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE,
                    PARAMETERIZED_REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE,
                    kind
            ));
        }

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .addTags(MoreTags.d(d))
                .setContent(content));
    }
}
