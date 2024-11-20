package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.Bech32;
import kotlin.Triple;

import java.util.ArrayList;
import java.util.List;

public final class Codecs {

    private static final List<Codec<?>> codecs = new ArrayList<>();

    static {
        register(new NoteCodec());
        register(new NpubCodec());
        register(new NsecCodec());
        register(new NprofileCodec());
        register(new NeventCodec());
        register(new NaddrCodec());
    }

    public static void register(Codec<?> codec) {
        codecs.add(codec);
    }

    public static <T> T decode(String bech32, Class<T> clazz) {
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

    public static <T> String encode(String hrp, T data) {
        try {
            byte[] bytes = codecs.stream()
                    .filter(it -> it.supports(hrp, data.getClass()))
                    .findFirst()
                    .map(it -> it.encode(hrp, data))
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported bech32 value"));

            return Bech32.encodeBytes(hrp, bytes, Bech32.Encoding.Bech32);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while encoding bech32", e);
        }
    }
}
