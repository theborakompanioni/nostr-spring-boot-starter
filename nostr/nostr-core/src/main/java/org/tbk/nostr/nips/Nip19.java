package org.tbk.nostr.nips;

import fr.acinq.bitcoin.Bech32;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import kotlin.Triple;
import lombok.*;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.util.MorePublicKeys;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/19.md">NIP-19</a>.
 */
public final class Nip19 {

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

    public static Naddr fromNaddr(String bech32) {
        return decode(bech32, Naddr.class);
    }

    @Value
    @Builder
    public static class Nprofile {
        @NonNull
        XonlyPublicKey publicKey;

        @Singular("relay")
        List<RelayUri> relays;
    }

    @Value
    @Builder
    public static class Nevent {
        @NonNull
        EventId id;

        @Singular("relay")
        List<RelayUri> relays;

        XonlyPublicKey publicKey;

        Kind kind;

        public Optional<XonlyPublicKey> getPublicKey() {
            return Optional.ofNullable(publicKey);
        }

        public Optional<Kind> getKind() {
            return Optional.ofNullable(kind);
        }
    }

    @Value
    @Builder
    public static class Naddr {
        @NonNull
        EventUri uri;

        @Singular("relay")
        List<RelayUri> relays;
    }

    @Getter
    @RequiredArgsConstructor
    private enum EntityType {
        NPUB("npub"),
        NSEC("nsec"),
        NOTE("note"),
        NPROFILE("nprofile"),
        NEVENT("nevent"),
        NADDR("naddr"),
        ;

        @NonNull
        private final String hrp;
    }

    @Getter
    @RequiredArgsConstructor
    private enum TlvType {
        SPECIAL(0),
        RELAY(1),
        AUTHOR(2),
        KIND(3);

        private final int value;
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
            return transformers.stream()
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

    private interface Transformer<T> {
        boolean supports(String hrp, Class<?> clazz);

        T decode(String hrp, byte[] data);
    }

    private static final List<Transformer<?>> transformers = List.of(new Transformer<EventId>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NOTE.getHrp().equals(hrp) && clazz.isAssignableFrom(EventId.class);
        }

        @Override
        public EventId decode(String hrp, byte[] data) {
            return EventId.of(data);
        }
    }, new Transformer<XonlyPublicKey>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NPUB.getHrp().equals(hrp) && clazz.isAssignableFrom(XonlyPublicKey.class);
        }

        @Override
        public XonlyPublicKey decode(String hrp, byte[] data) {
            XonlyPublicKey publicKey = MorePublicKeys.fromBytes(data);
            if (!publicKey.getPublicKey().isValid()) {
                throw new IllegalArgumentException("Invalid public key value");
            }
            return publicKey;
        }
    }, new Transformer<PrivateKey>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NSEC.getHrp().equals(hrp) && clazz.isAssignableFrom(PrivateKey.class);
        }

        @Override
        public PrivateKey decode(String hrp, byte[] data) {
            PrivateKey privateKey = new PrivateKey(new ByteVector32(data));
            if (!privateKey.isValid()) {
                throw new IllegalArgumentException("Invalid private key value");
            }
            return privateKey;
        }
    }, new Transformer<Nprofile>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NPROFILE.getHrp().equals(hrp) && clazz.isAssignableFrom(Nprofile.class);
        }

        @Override
        public Nprofile decode(String hrp, byte[] data) {
            List<TLV.Entry> entries = TLV.parse(data);

            TLV.Entry specialEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

            XonlyPublicKey publicKey = Optional.of(specialEntry.getValue())
                    .map(MorePublicKeys::fromBytes)
                    .filter(it -> it.getPublicKey().isValid())
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.SPECIAL.getValue())));

            List<RelayUri> relayEntries = entries.stream()
                    .filter(it -> it.getType() == TlvType.RELAY.getValue())
                    .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                    .map(RelayUri::tryFromString)
                    .flatMap(Optional::stream)
                    .toList();

            return Nprofile.builder()
                    .publicKey(publicKey)
                    .relays(relayEntries)
                    .build();
        }
    }, new Transformer<Nevent>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NEVENT.getHrp().equals(hrp) && clazz.isAssignableFrom(Nevent.class);
        }

        @Override
        public Nevent decode(String hrp, byte[] data) {
            List<TLV.Entry> entries = TLV.parse(data);

            TLV.Entry specialEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

            EventId eventId = EventId.of(specialEntry.getValue());

            List<RelayUri> relayEntries = entries.stream()
                    .filter(it -> it.getType() == TlvType.RELAY.getValue())
                    .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                    .map(RelayUri::tryFromString)
                    .flatMap(Optional::stream)
                    .toList();

            Optional<TLV.Entry> authorEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                    .findFirst();

            Optional<XonlyPublicKey> author = authorEntry
                    .map(TLV.Entry::getValue)
                    .map(MorePublicKeys::fromBytes)
                    .filter(it -> it.getPublicKey().isValid());

            Optional<TLV.Entry> kindEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.KIND.getValue())
                    .findFirst();

            Optional<Kind> kind = kindEntry
                    .map(TLV.Entry::getValue)
                    .map(it -> ByteBuffer.wrap(it).getInt())
                    .filter(Kind::isValidKind)
                    .map(Kind::of);

            return Nevent.builder()
                    .id(eventId)
                    .relays(relayEntries)
                    .publicKey(author.orElse(null))
                    .kind(kind.orElse(null))
                    .build();
        }
    }, new Transformer<Naddr>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NADDR.getHrp().equals(hrp) && clazz.isAssignableFrom(Naddr.class);
        }

        @Override
        public Naddr decode(String hrp, byte[] data) {
            List<TLV.Entry> entries = TLV.parse(data);

            TLV.Entry specialEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.SPECIAL.getValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.SPECIAL.getValue())));

            String dTagValue = new String(specialEntry.getValue(), StandardCharsets.UTF_8);

            List<RelayUri> relayEntries = entries.stream()
                    .filter(it -> it.getType() == TlvType.RELAY.getValue())
                    .map(it -> new String(it.getValue(), StandardCharsets.US_ASCII))
                    .map(RelayUri::tryFromString)
                    .flatMap(Optional::stream)
                    .toList();

            TLV.Entry authorEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.AUTHOR.getValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.AUTHOR.getValue())));

            XonlyPublicKey author = Optional.of(authorEntry.getValue())
                    .map(MorePublicKeys::fromBytes)
                    .filter(it -> it.getPublicKey().isValid())
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.AUTHOR.getValue())));

            TLV.Entry kindEntry = entries.stream()
                    .filter(it -> it.getType() == TlvType.KIND.getValue())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: No value with type %d.".formatted(TlvType.KIND.getValue())));

            Kind kind = Optional.of(kindEntry.getValue())
                    .map(it -> ByteBuffer.wrap(it).getInt())
                    .filter(Kind::isValidKind)
                    .map(Kind::of)
                    .orElseThrow(() -> new IllegalArgumentException("Decoding failed: Invalid value with type %d.".formatted(TlvType.KIND.getValue())));

            return Naddr.builder()
                    .uri(EventUri.of(kind, author.value.toHex(), dTagValue))
                    .relays(relayEntries)
                    .build();
        }
    });

    private static class TLV {
        static List<TLV.Entry> parse(byte[] raw) {
            int i = 0;

            List<Entry> entries = new LinkedList<>();
            while (i + 1 < raw.length) {
                int type = Byte.toUnsignedInt(raw[i]);
                int length = Byte.toUnsignedInt(raw[i + 1]);

                if (i + 2 + length > raw.length) {
                    break;
                }

                byte[] value = Arrays.copyOfRange(raw, i + 2, i + 2 + length);

                entries.add(Entry.builder()
                        .type(type)
                        .value(value)
                        .build());

                i = i + 2 + length;
            }

            return entries;
        }

        @Value
        @Builder
        static class Entry {
            int type;
            byte[] value;
        }
    }
}
