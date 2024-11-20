package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.PrivateKey;

class NsecCodec implements Codec<PrivateKey> {
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

    @Override
    public byte[] encode(String hrp, Object data) {
        if (!supports(hrp, data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((PrivateKey) data).value.toByteArray();
    }
}
