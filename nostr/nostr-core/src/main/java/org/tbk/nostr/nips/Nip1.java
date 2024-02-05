package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;

import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-1</a>.
 */
public final class Nip1 {
    private Nip1() {
        throw new UnsupportedOperationException();
    }

    public static Event.Builder createTextNote(XonlyPublicKey publicKey, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(1)
                .setContent(content));
    }

    public static Event.Builder createMetadata(XonlyPublicKey publicKey, Metadata metadata) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(0)
                .setContent(JsonWriter.toJson(metadata)));
    }
}
