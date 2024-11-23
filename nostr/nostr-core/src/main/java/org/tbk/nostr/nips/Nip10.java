package org.tbk.nostr.nips;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/10.md">NIP-10</a>.
 */
public final class Nip10 {
    private Nip10() {
        throw new UnsupportedOperationException();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Marker {
        REPLY("reply"),
        ROOT("root"),
        MENTION("mention");

        private final String value;
    }

    public static TagValue e(EventId eventId) {
        return e(eventId, "");
    }

    private static TagValue e(EventId eventId, String relayUrlOrEmpty) {
        return MoreTags.named(
                IndexedTag.e.name(),
                eventId.toHex(),
                relayUrlOrEmpty
        );
    }

    public static TagValue e(EventId eventId, RelayUri recommendedRelay) {
        return e(eventId, recommendedRelay == null ? "" : recommendedRelay.getUri().toString());
    }

    public static TagValue e(EventId eventId, Nip10.Marker marker) {
        return e(eventId, null, marker);
    }

    public static TagValue e(EventId eventId, @Nullable RelayUri recommendedRelay, Nip10.Marker marker) {
        return e(eventId, recommendedRelay).toBuilder()
                .addValues(marker.getValue())
                .build();
    }

    public static TagValue e(EventId eventId, @Nullable RelayUri recommendedRelay, Nip10.Marker marker, XonlyPublicKey publicKey) {
        return e(eventId, recommendedRelay, marker).toBuilder()
                .addValues(publicKey.value.toHex())
                .build();
    }

    public static TagValue p(XonlyPublicKey publicKey) {
        return p(publicKey, "");
    }

    private static TagValue p(XonlyPublicKey publicKey, String relayUrlOrEmpty) {
        return MoreTags.named(
                IndexedTag.p.name(),
                publicKey.value.toHex(),
                relayUrlOrEmpty
        );
    }

    public static TagValue p(XonlyPublicKey publicKey, RelayUri recommendedRelay) {
        return p(publicKey, recommendedRelay == null ? "" : recommendedRelay.getUri().toString());
    }
}
