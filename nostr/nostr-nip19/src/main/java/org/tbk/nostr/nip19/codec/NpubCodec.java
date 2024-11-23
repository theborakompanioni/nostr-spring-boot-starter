package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.nip19.Nip19Entity;
import org.tbk.nostr.nip19.Npub;
import org.tbk.nostr.util.MorePublicKeys;

public class NpubCodec implements Codec<Npub> {
    @Override
    public boolean supports(Class<? extends Nip19Entity> clazz) {
        return clazz.isAssignableFrom(Npub.class);
    }

    @Override
    public Npub decode(byte[] data) {
        XonlyPublicKey publicKey = MorePublicKeys.fromBytes(data);
        if (!publicKey.getPublicKey().isValid()) {
            throw new IllegalArgumentException("Invalid public key value");
        }
        return Npub.builder().publicKey(publicKey).build();
    }

    @Override
    public byte[] encode(Nip19Entity data) {
        if (!supports(data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((Npub) data).getPublicKey().value.toByteArray();
    }
}
