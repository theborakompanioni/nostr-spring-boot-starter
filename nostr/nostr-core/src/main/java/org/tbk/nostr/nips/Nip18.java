package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/18.md">NIP-18</a>.
 */
public final class Nip18 {
    private static final Kind REPOST_EVENT_KIND = Kind.of(6);
    private static final Kind GENERIC_REPOST_EVENT_KIND = Kind.of(16);

    private Nip18() {
        throw new UnsupportedOperationException();
    }

    public static Kind kindRepost() {
        return REPOST_EVENT_KIND;
    }

    public static Kind kindGenericRepost() {
        return GENERIC_REPOST_EVENT_KIND;
    }

    public static boolean isRepostEvent(Event event) {
        return event.getKind() == Nip18.kindRepost().getValue() ||
               event.getKind() == Nip18.kindGenericRepost().getValue();
    }

    public static Event.Builder repost(XonlyPublicKey publicKey, Event event, RelayUri relayUri) {
        return event.getKind() == 1 ? repostShortTextNote(publicKey, event, relayUri) : repostGenericEvent(publicKey, event, relayUri);
    }

    public static Event.Builder repostShortTextNote(XonlyPublicKey publicKey, Event event, RelayUri relayUri) {
        if (event.getKind() != 1) {
            throw new IllegalArgumentException("Can only repost short text notes. Expected kind 1, but got %d.".formatted(event.getKind()));
        }
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kindRepost().getValue())
                .setContent(JsonWriter.toJson(event))
                .addTags(MoreTags.e(event, relayUri))
                .addTags(MoreTags.p(event)));
    }

    public static Event.Builder repostGenericEvent(XonlyPublicKey publicKey, Event event, RelayUri relayUri) {
        if (event.getKind() == 1) {
            throw new IllegalArgumentException("Can only repost events other than short text notes. Expected kind != 1, but got %d.".formatted(event.getKind()));
        }
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(kindGenericRepost().getValue())
                .setContent(JsonWriter.toJson(event))
                .addTags(MoreTags.e(event, relayUri))
                .addTags(MoreTags.p(event))
                .addTags(MoreTags.k(event)));
    }
}
