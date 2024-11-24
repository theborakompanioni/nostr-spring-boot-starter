package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreKinds;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;
import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-1</a>.
 */
public final class Nip1 {

    private Nip1() {
        throw new UnsupportedOperationException();
    }

    public static boolean isReplaceableEvent(EventOrBuilder event) {
        return MoreKinds.isReplaceable(event.getKind());
    }

    public static boolean isEphemeralEvent(EventOrBuilder event) {
        return MoreKinds.isEphemeral(event.getKind());
    }

    public static boolean isAddressableEvent(EventOrBuilder event) {
        return MoreKinds.isAddressable(event.getKind());
    }

    public static Event.Builder createMetadata(XonlyPublicKey publicKey, Metadata metadata) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindProfileMetadata.getValue())
                .setContent(JsonWriter.toJson(metadata)));
    }

    public static Event.Builder createTextNote(XonlyPublicKey publicKey, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindTextNote.getValue())
                .setContent(content));
    }

    public static Event.Builder createReplaceableEvent(XonlyPublicKey publicKey, String content) {
        return createReplaceableEvent(publicKey, content, MoreKinds.kindReplaceableRange().lowerEndpoint().getValue());
    }

    public static Event.Builder createReplaceableEvent(XonlyPublicKey publicKey, String content, int kind) {
        MoreKinds.checkReplaceable(kind);

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .setContent(content));
    }

    public static Event.Builder createEphemeralEvent(XonlyPublicKey publicKey, String content) {
        return createEphemeralEvent(publicKey, content, MoreKinds.kindEphemeralRange().lowerEndpoint().getValue());
    }

    public static Event.Builder createEphemeralEvent(XonlyPublicKey publicKey, String content, int kind) {
        MoreKinds.checkEphemeral(kind);

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .setContent(content));
    }


    public static Event.Builder createAddressableEvent(XonlyPublicKey publicKey, String content, @Nullable String dTagValue) {
        return createAddressableEvent(publicKey, content, MoreKinds.kindAddressableRange().lowerEndpoint().getValue(), dTagValue);
    }

    public static Event.Builder createAddressableEvent(XonlyPublicKey publicKey, String content, int kind, @Nullable String dTagValue) {
        MoreKinds.checkAddressable(kind);

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kind)
                .addTags(dTagValue == null ? MoreTags.d() : MoreTags.d(dTagValue))
                .setContent(content));
    }
}
