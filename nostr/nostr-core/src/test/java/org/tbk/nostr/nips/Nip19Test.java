package org.tbk.nostr.nips;

import fr.acinq.bitcoin.Bech32;
import fr.acinq.bitcoin.XonlyPublicKey;
import kotlin.Triple;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Nip19Test {

    @Test
    void itShouldDecodeNpubSuccessfully() {
        XonlyPublicKey publicKey0 = Nip19.fromNpub("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg");
        assertThat(publicKey0.value.toHex(), is("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));

        String odellNpub = "npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx";
        XonlyPublicKey publicKey1 = Nip19.fromNpub(odellNpub);
        assertThat(publicKey1.value.toHex(), is("04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9"));
    }

    @Test
    void itShouldDecodeNpubFailure() {
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> Nip19.fromNpub("npub10"));
        assertThat(e0.getMessage(), is("Error while decoding bech32"));
        assertThat(e0.getCause().getMessage(), startsWith("invalid checksum for npub1"));

        String invalidPublicKeyNpub = Nip19.toNpub(MorePublicKeys.fromHex("00".repeat(32)));
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> Nip19.fromNpub(invalidPublicKeyNpub));
        assertThat(e1.getMessage(), is("Error while decoding bech32"));
        assertThat(e1.getCause().getMessage(), is("Invalid public key value"));
    }

    @Test
    void itShouldEncodeNpubSuccessfully() {
        String npub0 = Nip19.toNpub(MorePublicKeys.fromHex("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));
        assertThat(npub0, is("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"));

        String npub1 = Nip19.toNpub(MorePublicKeys.fromHex("04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9"));
        assertThat(npub1, is("npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx"));
    }

    private static final class Nip19 {

        public static XonlyPublicKey fromNpub(String bech32) {
            return decode(bech32, XonlyPublicKey.class);
        }

        public static String toNpub(XonlyPublicKey publicKey) {
            return encode(EntityType.NPUB, publicKey.value.toByteArray());
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
        });

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
    }
}
