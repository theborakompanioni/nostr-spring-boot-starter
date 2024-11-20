package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.Bech32;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import kotlin.Triple;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.*;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/19.md">NIP-19</a>.
 */
public final class Nip19 {

    private static final List<Codec<?>> codecs;

    static {
        codecs = List.of(
                new NoteCodec(),
                new NpubCodec(),
                new NsecCodec(),
                new NprofileCodec(),
                new NeventCodec(),
                new NaddrCodec());
    }

    private Nip19() {
        throw new UnsupportedOperationException();
    }

    public static XonlyPublicKey fromNpub(String bech32) {
        return decode(bech32, XonlyPublicKey.class);
    }

    public static String toNpub(XonlyPublicKey data) {
        return encode(EntityType.NPUB, data.value.toByteArray());
    }

    public static PrivateKey fromNsec(String bech32) {
        return decode(bech32, PrivateKey.class);
    }

    public static String toNsec(PrivateKey data) {
        return encode(EntityType.NSEC, data.value.toByteArray());
    }

    public static EventId fromNote(String bech32) {
        return decode(bech32, EventId.class);
    }

    public static String toNote(EventId data) {
        return encode(EntityType.NOTE, data.toByteArray());
    }

    public static Nprofile fromNprofile(String bech32) {
        return decode(bech32, Nprofile.class);
    }

    public static Nevent fromNevent(String bech32) {
        return decode(bech32, Nevent.class);
    }

    public static String toNevent(Nevent nevent) {
        List<TLV.Entry> entries = new LinkedList<>();
        entries.add(TLV.Entry.builder().type(TlvType.SPECIAL.getValue()).value(nevent.getEventId().toByteArray()).build());
        nevent.getRelays().forEach(it -> {
            entries.add(TLV.Entry.builder().type(TlvType.RELAY.getValue()).value(it.getUri().toASCIIString().getBytes()).build());
        });
        nevent.getPublicKey().ifPresent(it -> {
            entries.add(TLV.Entry.builder().type(TlvType.AUTHOR.getValue()).value(it.value.toByteArray()).build());
        });
        nevent.getKind().ifPresent(it -> {
            entries.add(TLV.Entry.builder().type(TlvType.KIND.getValue()).value(Ints.toByteArray(it.getValue())).build());
        });
        return encode(EntityType.NEVENT, TLV.encode(entries));
    }

    public static Naddr fromNaddr(String bech32) {
        return decode(bech32, Naddr.class);
    }

    public static String toNevent(Event event) {
        return toNevent(event, Collections.emptyList());
    }

    public static String toNevent(Event event, Collection<RelayUri> relays) {
        return toNevent(Nevent.builder()
                .eventId(EventId.of(event.getId().toByteArray()))
                .relays(relays)
                .publicKey(MorePublicKeys.fromEvent(event))
                .kind(Kind.of(event.getKind()))
                .build());
    }

    private static byte[] decode(String bech32) {
        try {
            return Bech32.decodeBytes(bech32, false).component2();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decoding bech32", e);
        }
    }

    private static <T> Optional<T> tryDecode(String bech32, Class<T> clazz) {
        try {
            return Optional.ofNullable(decode(bech32, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static <T> T decode(String bech32, Class<T> clazz) {
        try {
            Triple<String, byte[], Bech32.Encoding> decoded = Bech32.decodeBytes(bech32, false);

            String hrp = decoded.component1();
            return codecs.stream()
                    .filter(it -> it.supports(hrp, clazz))
                    .findFirst()
                    .map(it -> it.decode(hrp, decoded.component2()))
                    .map(clazz::cast)
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported bech32 value"));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decoding bech32", e);
        }
    }

    private static String encode(EntityType type, byte[] data) {
        try {
            return Bech32.encodeBytes(type.getHrp(), data, Bech32.Encoding.Bech32);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while encoding bech32", e);
        }
    }
}
