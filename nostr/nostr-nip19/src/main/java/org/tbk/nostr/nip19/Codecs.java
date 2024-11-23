package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.Bech32;
import kotlin.Triple;
import org.tbk.nostr.nip19.codec.*;

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
        codecs.addFirst(codec);
    }

    public static Nip19Entity decode(String bech32) {
        try {
            Triple<String, byte[], Bech32.Encoding> decoded = Bech32.decodeBytes(bech32, false);
            return decode(decoded.component2(), Nip19Type.fromHrp(decoded.component1()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while decoding bech32", e);
        }
    }

    private static Nip19Entity decode(byte[] bytes, Nip19Type entityType) {
        Class<? extends Nip19Entity> clazz = switch (entityType) {
            case NPUB -> Npub.class;
            case NSEC -> Nsec.class;
            case NOTE -> Note.class;
            case NPROFILE -> Nprofile.class;
            case NEVENT -> Nevent.class;
            case NADDR -> Naddr.class;
        };

        return decode(bytes, clazz);
    }

    private static Nip19Entity decode(byte[] bytes, Class<? extends Nip19Entity> clazz) {
        return codecs.stream()
                .filter(it -> it.supports(clazz))
                .findFirst()
                .map(it -> it.decode(bytes))
                .map(clazz::cast)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported bech32 value"));
    }

    public static String encode(Nip19Entity data) {
        try {
            byte[] bytes = codecs.stream()
                    .filter(it -> it.supports(data.getClass()))
                    .findFirst()
                    .map(it -> it.encode(data))
                    .orElseThrow(() -> new IllegalArgumentException("Unsupported bech32 value"));

            return Bech32.encodeBytes(data.getEntityType().getHrp(), bytes, Bech32.Encoding.Bech32);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while encoding bech32", e);
        }
    }
}
