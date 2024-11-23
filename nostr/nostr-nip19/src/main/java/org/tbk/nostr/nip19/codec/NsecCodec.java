package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.PrivateKey;
import org.tbk.nostr.nip19.Nip19Entity;
import org.tbk.nostr.nip19.Nsec;

public class NsecCodec implements Codec<Nsec> {
    @Override
    public boolean supports(Class<? extends Nip19Entity> clazz) {
        return clazz.isAssignableFrom(Nsec.class);
    }

    @Override
    public Nsec decode(byte[] data) {
        PrivateKey privateKey = new PrivateKey(new ByteVector32(data));
        if (!privateKey.isValid()) {
            throw new IllegalArgumentException("Invalid private key value");
        }
        return Nsec.builder().privateKey(privateKey).build();
    }

    @Override
    public byte[] encode(Nip19Entity data) {
        if (!supports(data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((Nsec) data).getPrivateKey().value.toByteArray();
    }
}
