package org.tbk.nostr.nip44;

import javax.crypto.Cipher;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.GeneralSecurityException;

final class ChaCha20 {

    private ChaCha20() {
        throw new UnsupportedOperationException();
    }

    static byte[] encrypt(byte[] key, byte[] nonce, byte[] input) {
        try {
            return chacha20(key, nonce, input, Cipher.ENCRYPT_MODE);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    static byte[] decrypt(byte[] key, byte[] nonce, byte[] input) {
        try {
            return chacha20(key, nonce, input, Cipher.DECRYPT_MODE);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] chacha20(byte[] key, byte[] nonce, byte[] input, int opmode) throws GeneralSecurityException {
        if (key.length != 32) {
            throw new IllegalArgumentException("Invalid ChaCha20 key: Must be 32 bytes, got %d.".formatted(key.length));
        }
        if (nonce.length != 12) {
            throw new IllegalArgumentException("Invalid ChaCha20 nonce: Must be 12 bytes, got %d.".formatted(nonce.length));
        }
        Cipher cipher = Cipher.getInstance("ChaCha20");
        ChaCha20ParameterSpec spec = new ChaCha20ParameterSpec(nonce, 0);
        cipher.init(opmode, new SecretKeySpec(key, "ChaCha20"), spec);
        return cipher.doFinal(input);
    }
}
