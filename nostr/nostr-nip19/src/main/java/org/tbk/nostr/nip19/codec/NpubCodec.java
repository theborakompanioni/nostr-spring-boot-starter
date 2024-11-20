package org.tbk.nostr.nip19.codec;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.nip19.EntityType;
import org.tbk.nostr.util.MorePublicKeys;

public class NpubCodec implements Codec<XonlyPublicKey> {
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

    @Override
    public byte[] encode(String hrp, Object data) {
        if (!supports(hrp, data.getClass())) {
            throw new IllegalArgumentException("Unsupported argument types");
        }
        return ((XonlyPublicKey) data).value.toByteArray();
    }
}
