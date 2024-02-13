package org.tbk.nostr.util;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nips.Nip10;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.nips.Nip40;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class MoreTags {

    private MoreTags() {
        throw new UnsupportedOperationException();
    }

    public static List<TagValue> findByName(EventOrBuilder event, String name) {
        return event.getTagsList().stream()
                .filter(it -> it.getName().equals(name))
                .toList();
    }

    public static Optional<TagValue> findByNameSingle(EventOrBuilder event, String name) {
        TagValue found = null;
        for (TagValue tag : event.getTagsList()) {
            if (tag.getName().equals(name)) {
                if (found != null) {
                    return Optional.empty();
                }
                found = tag;
            }
        }
        return Optional.ofNullable(found);
    }

    public static TagValue e(Event event) {
        return e(EventId.of(event.getId().toByteArray()));
    }

    public static TagValue e(Event event, Nip10.Marker marker) {
        return e(EventId.of(event.getId().toByteArray()), marker);
    }

    public static TagValue e(Event event, RelayUri recommendedRelay) {
        return e(EventId.of(event.getId().toByteArray()), recommendedRelay);
    }

    public static TagValue e(Event event, @Nullable RelayUri recommendedRelay, Nip10.Marker marker) {
        return e(EventId.of(event.getId().toByteArray()), recommendedRelay, marker);
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

    public static TagValue d(String... values) {
        return named("d", values);
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

    public static TagValue a(int kind, XonlyPublicKey publicKey) {
        return a("%d:%s".formatted(kind, publicKey.value.toHex()));
    }

    public static TagValue a(int kind, XonlyPublicKey publicKey, String dTagValue) {
        return a("%d:%s:%s".formatted(kind, publicKey.value.toHex(), dTagValue));
    }

    public static TagValue a(int kind, XonlyPublicKey publicKey, RelayUri recommendedRelay) {
        return a("%d:%s".formatted(kind, publicKey), recommendedRelay.getUri().toString());
    }

    public static TagValue a(int kind, XonlyPublicKey publicKey, String dTagValue, RelayUri recommendedRelay) {
        return a("%d:%s%s".formatted(kind, publicKey, dTagValue), recommendedRelay.getUri().toString());
    }

    /**
     * Prefer typed versions, e.g. {@link #a(int, XonlyPublicKey)}, {@link #a(int, XonlyPublicKey, RelayUri)},
     */
    public static TagValue a(String... values) {
        return named("a", values);
    }

    public static TagValue expiration(Duration duration) {
        return Nip40.expiration(duration);
    }

    public static TagValue expiration(Instant instant) {
        return Nip40.expiration(instant);
    }

    public static TagValue nonce(String nonce, long targetDifficulty) {
        return Nip13.nonce(nonce, targetDifficulty);
    }

    public static TagValue named(String name, String... values) {
        return TagValue.newBuilder().setName(name)
                .addAllValues(Arrays.asList(values))
                .build();
    }
}
