package org.tbk.nostr.util;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nips.*;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagFilter;
import org.tbk.nostr.proto.TagValue;

import javax.annotation.Nullable;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class MoreTags {

    private MoreTags() {
        throw new UnsupportedOperationException();
    }

    public static List<TagValue> findByName(EventOrBuilder event, IndexedTag tag) {
        return findByName(event, tag.name());
    }

    public static List<TagValue> findByName(EventOrBuilder event, String name) {
        return event.getTagsList().stream()
                .filter(it -> it.getName().equals(name))
                .toList();
    }

    public static Optional<TagValue> findByNameSingle(EventOrBuilder event, IndexedTag tag) {
        return findByNameSingle(event, tag.name());
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

    public static Optional<TagValue> findByNameFirst(EventOrBuilder event, IndexedTag tag) {
        return findByNameFirst(event, tag.name());
    }

    public static Optional<TagValue> findByNameFirst(EventOrBuilder event, String name) {
        return event.getTagsList().stream()
                .filter(it -> it.getName().equals(name))
                .findFirst();
    }

    public static TagValue e(Event event) {
        return e(EventId.of(event.getId().toByteArray()));
    }

    public static TagValue e(Event event, RelayUri recommendedRelay) {
        return e(EventId.of(event.getId().toByteArray()), recommendedRelay);
    }

    public static TagValue e(Event event, Nip10.Marker marker) {
        return e(EventId.of(event.getId().toByteArray()), null, marker);
    }

    public static TagValue e(Event event, @Nullable RelayUri recommendedRelay, Nip10.Marker marker) {
        return e(EventId.of(event.getId().toByteArray()), recommendedRelay, marker);
    }

    public static TagValue e(Event event, @Nullable RelayUri recommendedRelay, Nip10.Marker marker, XonlyPublicKey publicKey) {
        return Nip10.e(EventId.of(event.getId().toByteArray()), recommendedRelay, marker, publicKey);
    }

    public static TagValue e(EventId eventId) {
        return Nip10.e(eventId);
    }

    public static TagValue e(EventId eventId, RelayUri recommendedRelay) {
        return Nip10.e(eventId, recommendedRelay);
    }

    public static TagValue e(EventId eventId, Nip10.Marker marker) {
        return Nip10.e(eventId, null, marker);
    }

    public static TagValue e(EventId eventId, @Nullable RelayUri recommendedRelay, Nip10.Marker marker) {
        return Nip10.e(eventId, recommendedRelay, marker);
    }

    public static TagValue e(EventId eventId, @Nullable RelayUri recommendedRelay, Nip10.Marker marker, XonlyPublicKey publicKey) {
        return Nip10.e(eventId, recommendedRelay, marker, publicKey);
    }

    /**
     * Prefer typed versions, e.g. {@link #e(EventId)}, {@link #e(EventId, RelayUri)},
     */
    public static TagValue e(String... values) {
        return named(IndexedTag.e.name(), values);
    }

    public static TagValue p(Event event) {
        return p(MorePublicKeys.fromEvent(event));
    }

    public static TagValue p(XonlyPublicKey publicKey) {
        return Nip10.p(publicKey);
    }

    public static TagValue p(XonlyPublicKey publicKey, RelayUri recommendedRelay) {
        return Nip10.p(publicKey, recommendedRelay);
    }

    /**
     * Prefer typed versions, e.g. {@link #p(XonlyPublicKey)} , {@link #p(XonlyPublicKey, RelayUri)},
     */
    public static TagValue p(String... values) {
        return named(IndexedTag.p.name(), values);
    }

    public static TagValue a(Event event) {
        if (Nip1.isReplaceableEvent(event)) {
            return a(event.getKind(), MorePublicKeys.fromEvent(event));
        }
        if (Nip1.isAddressableEvent(event)) {
            TagValue dTag = MoreTags.findByNameSingle(event, IndexedTag.d)
                    .filter(it -> it.getValuesCount() >= 1)
                    .orElseThrow(() -> new IllegalArgumentException("Missing or conflicting '%s' tag.".formatted(IndexedTag.d)));
            return a(event.getKind(), MorePublicKeys.fromEvent(event), dTag.getValues(0));
        }

        throw new IllegalArgumentException("Expected a replaceable or addressable event. Got kind: %d".formatted(event.getKind()));
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
        return a("%d:%s:%s".formatted(kind, publicKey, dTagValue), recommendedRelay.getUri().toString());
    }

    /**
     * Prefer typed versions, e.g. {@link #a(int, XonlyPublicKey)}, {@link #a(int, XonlyPublicKey, RelayUri)},
     */
    public static TagValue a(String... values) {
        return named(IndexedTag.a.name(), values);
    }

    public static TagValue d(String... values) {
        return named(IndexedTag.d.name(), values);
    }

    public static TagValue k(Event event) {
        return k(event.getKind());
    }

    public static TagValue k(Kind kind) {
        return k(kind.getValue());
    }

    public static TagValue k(int kind) {
        return named(IndexedTag.k.name(), String.valueOf(kind));
    }

    public static TagValue q(EventId eventId, RelayUri recommendedRelay, XonlyPublicKey publicKey) {
        return named(IndexedTag.q.name(), eventId.toHex(), recommendedRelay.getUri().toString(), publicKey.value.toHex());
    }

    public static TagValue r(URI uri) {
        return named(IndexedTag.r.name(), uri.toString());
    }

    public static TagValue r(RelayUri relay) {
        return Nip65.r(relay);
    }

    public static TagValue r(RelayUri relay, Nip65.Marker marker) {
        return Nip65.r(relay, marker);
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

    public static TagFilter filter(IndexedTag tag, String... values) {
        return filter(tag, Arrays.stream(values).toList());
    }

    public static TagFilter filter(IndexedTag tag, List<String> values) {
        if (values.isEmpty()) {
            throw new IllegalArgumentException("A tag filter must have at least one value.");
        }
        return TagFilter.newBuilder()
                .setName(tag.name())
                .addAllValues(values)
                .build();
    }

    /**
     * Create a {@link TagFilter} from a {@link TagValue}
     *
     * @param tag a tag value (must be an indexed tag, see {@link IndexedTag})
     * @return a tag filter matching the first value of the given tag
     */
    public static TagFilter filter(TagValue tag) {
        return filter(IndexedTag.valueOf(tag.getName()), tag.getValuesList().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tag must have a value")));
    }
}
