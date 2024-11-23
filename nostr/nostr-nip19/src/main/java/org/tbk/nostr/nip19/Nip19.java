package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.*;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;

import java.util.Collection;
import java.util.Collections;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/19.md">NIP-19</a>.
 */
public final class Nip19 {

    private Nip19() {
        throw new UnsupportedOperationException();
    }

    public static String encode(Nip19Entity data) {
        return Codecs.encode(data);
    }

    public static Nip19Entity decode(String bech32) {
        return Codecs.decode(bech32);
    }

    public static <T extends Nip19Entity> T decodeStrict(String bech32, Class<T> clazz) {
        Nip19Entity decoded = Codecs.decode(bech32);
        if (clazz.isAssignableFrom(decoded.getClass())) {
            return clazz.cast(decoded);
        }
        throw new IllegalArgumentException("Could not decode to %s".formatted(clazz.getSimpleName()));
    }

    public static Npub decodeNpub(String bech32) {
        return decodeStrict(bech32, Npub.class);
    }

    public static String encodeNpub(XonlyPublicKey publicKey) {
        return encode(Npub.builder().publicKey(publicKey).build());
    }

    public static Nsec decodeNsec(String bech32) {
        return decodeStrict(bech32, Nsec.class);
    }

    public static String encodeNsec(PrivateKey privateKey) {
        return encode(Nsec.builder().privateKey(privateKey).build());
    }

    public static Note decodeNote(String bech32) {
        return decodeStrict(bech32, Note.class);
    }

    public static String encodeNote(EventId eventId) {
        return encode(Note.builder().eventId(eventId).build());
    }

    public static Nprofile decodeNprofile(String bech32) {
        return decodeStrict(bech32, Nprofile.class);
    }

    public static String encodeNprofile(XonlyPublicKey publicKey) {
        return encodeNprofile(publicKey, Collections.emptyList());
    }

    public static String encodeNprofile(XonlyPublicKey publicKey, Collection<RelayUri> relays) {
        return encode(Nprofile.builder()
                .publicKey(publicKey)
                .relays(relays)
                .build());
    }

    public static Nevent decodeNevent(String bech32) {
        return decodeStrict(bech32, Nevent.class);
    }

    public static String encodeNevent(Event event) {
        return encodeNevent(event, Collections.emptyList());
    }

    public static String encodeNevent(Event event, Collection<RelayUri> relays) {
        return encode(Nevent.builder()
                .eventId(EventId.of(event.getId().toByteArray()))
                .relays(relays)
                .publicKey(MorePublicKeys.fromEvent(event))
                .kind(Kind.of(event.getKind()))
                .build());
    }

    public static Naddr decodeNaddr(String bech32) {
        return decodeStrict(bech32, Naddr.class);
    }

    public static String encodeNaddr(EventUri eventUri) {
        return encodeNaddr(eventUri, Collections.emptyList());
    }

    public static String encodeNaddr(EventUri eventUri, Collection<RelayUri> relays) {
        return encode(Naddr.builder()
                .eventUri(eventUri)
                .relays(relays)
                .build());
    }

    public static String encodeNaddr(Event event) {
        if (!Nip1.isReplaceableEvent(event) && !Nip1.isAddressableEvent(event)) {
            throw new IllegalArgumentException("Event must be replaceable or addressable: Got kind %d.".formatted(event.getKind()));
        }

        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);

        if (Nip1.isReplaceableEvent(event)) {
            return encodeNaddr(EventUri.of(event.getKind(), publicKey.value.toHex()));
        }

        String dTagValue = MoreTags.findByNameSingle(event, IndexedTag.d)
                .filter(it -> it.getValuesCount() > 0)
                .map(it -> it.getValues(0))
                .orElseThrow(() -> new IllegalStateException("Missing or conflicting '%s' tag.".formatted(IndexedTag.d)));

        return encodeNaddr(EventUri.of(event.getKind(), publicKey.value.toHex(), dTagValue));
    }
}
