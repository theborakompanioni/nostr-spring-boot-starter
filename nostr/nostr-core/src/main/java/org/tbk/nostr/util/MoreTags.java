package org.tbk.nostr.util;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nips.Nip10;
import org.tbk.nostr.proto.TagValue;

import javax.annotation.Nullable;
import java.util.Arrays;

public final class MoreTags {

    private MoreTags() {
        throw new UnsupportedOperationException();
    }

    public static TagValue e(EventId eventId) {
        return named("e", eventId.toHex());
    }

    public static TagValue e(EventId eventId, Nip10.Marker marker) {
        return marker.tag(eventId);
    }

    public static TagValue e(EventId eventId, RelayUri recommendedRelay) {
        return named("e", eventId.toHex(), recommendedRelay.getUri().toString());
    }

    public static TagValue e(EventId eventId, @Nullable RelayUri recommendedRelay, Nip10.Marker marker) {
        return marker.tag(eventId, recommendedRelay);
    }

    /**
     * Prefer typed versions, e.g. {@link #e(EventId)}, {@link #e(EventId, RelayUri)},
     */
    public static TagValue e(String... values) {
        return named("e", values);
    }


    public static TagValue p(XonlyPublicKey publicKey) {
        return named("p", publicKey.value.toHex());
    }

    public static TagValue p(XonlyPublicKey publicKey, RelayUri recommendedRelay) {
        return named("p", publicKey.value.toHex(), recommendedRelay.getUri().toString());
    }

    /**
     * Prefer typed versions, e.g. {@link #p(XonlyPublicKey)} , {@link #p(XonlyPublicKey, RelayUri)},
     */
    public static TagValue p(String... values) {
        return named("p", values);
    }

    public static TagValue named(String name, String... values) {
        return TagValue.newBuilder().setName(name)
                .addAllValues(Arrays.asList(values))
                .build();
    }
}
