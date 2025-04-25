package org.tbk.nostr.nip44;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

final class Hmac {
    private Hmac() {
        throw new UnsupportedOperationException();
    }

    static byte[] hmacSha256(byte[] key, byte[]... data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKeySpec);
            for (byte[] bytes : data) {
                mac.update(bytes, 0, bytes.length);
            }
            return mac.doFinal();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }
}
