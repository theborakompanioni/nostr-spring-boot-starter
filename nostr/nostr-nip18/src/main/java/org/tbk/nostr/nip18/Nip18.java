package org.tbk.nostr.nip18;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip19.Nevent;
import org.tbk.nostr.nip19.Nip19Entity;
import org.tbk.nostr.nip21.NostrUri;
import org.tbk.nostr.nips.Nip10;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreKinds;
import org.tbk.nostr.util.MorePublicKeys;
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

    public static Event.Builder quote(XonlyPublicKey publicKey, Event event, RelayUri relayUri, String text) {
        return quote(publicKey, event, relayUri, text, Nevent.builder()
                .eventId(EventId.of(event.getId().toByteArray()))
                .relay(relayUri)
                .build());
    }

    public static Event.Builder quote(XonlyPublicKey publicKey, Event event, RelayUri relayUri, String text, Nip19Entity entity) {
        String content = switch (entity.getEntityType()) {
            case NPUB, NSEC, NPROFILE ->
                    throw new IllegalArgumentException("Can only add nevent, naddr or note, got: %s.".formatted(entity.getEntityType()));
            case NEVENT, NADDR, NOTE -> "%s\n\n%s".formatted(text, NostrUri.of(entity).getUri().toString());
        };

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(MoreKinds.kindShortTextNote().getValue())
                .setContent(content)
                .addTags(MoreTags.q(EventId.of(event.getId().toByteArray()), relayUri, MorePublicKeys.fromEvent(event)))
                .addTags(MoreTags.e(event, relayUri, Nip10.Marker.ROOT, MorePublicKeys.fromEvent(event)))
                .addTags(MoreTags.p(event)));
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
                .addTags(MoreTags.e(event, relayUri, Nip10.Marker.ROOT, MorePublicKeys.fromEvent(event)))
                .addTags(MoreTags.p(event)));
    }

    public static Event.Builder repostGenericEvent(XonlyPublicKey publicKey, Event event) {
        return repostGenericEvent(publicKey, event, null);
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
                .addTags(MoreTags.e(event, relayUri, Nip10.Marker.ROOT, MorePublicKeys.fromEvent(event)))
                .addTags(MoreTags.p(event))
                .addTags(MoreTags.k(event)));
    }
}
