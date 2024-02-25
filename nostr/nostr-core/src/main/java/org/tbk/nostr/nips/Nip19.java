package org.tbk.nostr.nips;

import fr.acinq.bitcoin.Bech32;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import kotlin.Triple;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.List;
import java.util.Optional;

public final class Nip19 {

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

    @Getter
    @RequiredArgsConstructor
    public enum EntityType {
        NPUB("npub"),
        NSEC("nsec"),
        NOTE("note");

        @NonNull
        private final String hrp;
    }

    public static byte[] decode(String bech32) {
        try {
            return Bech32.decodeBytes(bech32, false).component2();
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decoding bech32", e);
        }
    }

    public static <T> Optional<T> tryDecode(String bech32, Class<T> clazz) {
        try {
            return Optional.ofNullable(decode(bech32, clazz));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static <T> T decode(String bech32, Class<T> clazz) {
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

    public static String encode(EntityType type, byte[] data) {
        try {
            return Bech32.encodeBytes(type.getHrp(), data, Bech32.Encoding.Bech32);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while encoding bech32", e);
        }
    }

    interface Transformer<T> {
        boolean supports(String hrp, Class<?> clazz);

        T decode(String hrp, byte[] data);
    }

    private static final List<Transformer<?>> transformers = List.of(new Transformer<XonlyPublicKey>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NPUB.getHrp().equals(hrp) && clazz.isAssignableFrom(XonlyPublicKey.class);
        }

        @Override
        public XonlyPublicKey decode(String hrp, byte[] data) {
            if (!MorePublicKeys.isValidPublicKey(data)) {
                throw new IllegalArgumentException("Invalid public key value");
            }
            return MorePublicKeys.fromBytes(data);
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
    }, new Transformer<EventId>() {
        @Override
        public boolean supports(String hrp, Class<?> clazz) {
            return EntityType.NOTE.getHrp().equals(hrp) && clazz.isAssignableFrom(EventId.class);
        }

        @Override
        public EventId decode(String hrp, byte[] data) {
            return EventId.of(data);
        }
    });
}
