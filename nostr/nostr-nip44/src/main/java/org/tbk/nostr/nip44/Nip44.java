package org.tbk.nostr.nip44;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.PublicKey;
import fr.acinq.bitcoin.XonlyPublicKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public final class Nip44 {
    private static final byte VERSION_V2 = 0x02;
    private static final int MAC_SIZE = 32;
    private static final int NONCE_SIZE = 32;
    private static final int HKDF_EXPAND_LENGTH = 76;
    private static final int VERSION_SIZE = 1;
    private static final int METADATA_SIZE = VERSION_SIZE + NONCE_SIZE + MAC_SIZE;
    private static final int PLAINTEXT_MIN_SIZE = 1;
    private static final int PLAINTEXT_MAX_SIZE = 65_535;
    private static final int DATA_MIN_SIZE = METADATA_SIZE + 32 + 2;
    private static final int DATA_MAX_SIZE = METADATA_SIZE + PLAINTEXT_MAX_SIZE + 2 + 1;
    private static final int PAYLOAD_MIN_SIZE = 132;
    private static final int PAYLOAD_MAX_SIZE = 87_472;
    private static final byte[] NIP44_V2_BYTES = "nip44-v2".getBytes(StandardCharsets.UTF_8);
    private static final char NON_BASE64_FLAG = '#';

    private Nip44() {
        throw new UnsupportedOperationException();
    }

    public static PrivateKey getConversationKey(PrivateKey privateKey, XonlyPublicKey publicKey) {
        PublicKey shared = publicKey.getPublicKey().times(privateKey);
        return new PrivateKey(Hkdf.extract(shared.xOnly(), NIP44_V2_BYTES));
    }

    public static String encrypt(PrivateKey conversationKey, String plaintext) {
        return encrypt(conversationKey, plaintext, randomNonce());
    }

    public static String encrypt(PrivateKey conversationKey, String plaintext, byte[] nonce) {
        if (nonce.length != NONCE_SIZE) {
            throw new IllegalArgumentException("Invalid nonce: Must be 32 bytes, got %d.".formatted(nonce.length));
        }

        byte[][] keys = toMessageKeys(conversationKey, nonce);
        byte[] chachaKey = keys[0];
        byte[] chachaNonce = keys[1];
        byte[] hmacKey = keys[2];

        byte[] padded = pad(plaintext.getBytes(StandardCharsets.UTF_8));
        byte[] ciphertext = ChaCha20.encrypt(chachaKey, chachaNonce, padded);
        byte[] mac = Hmac.hmacSha256(hmacKey, nonce, ciphertext);
        byte[] raw = concat(new byte[]{VERSION_V2}, nonce, ciphertext, mac);

        return Base64.getEncoder().encodeToString(raw);
    }

    public static String decrypt(PrivateKey conversationKey, String payload) {
        byte[][] decodedPayload = decodePayload(payload);
        byte[] nonce = decodedPayload[0];
        byte[] ciphertext = decodedPayload[1];
        byte[] mac = decodedPayload[2];

        byte[][] keys = toMessageKeys(conversationKey, nonce);
        byte[] chachaKey = keys[0];
        byte[] chachaNonce = keys[1];
        byte[] hmacKey = keys[2];

        byte[] calculatedMac = Hmac.hmacSha256(hmacKey, nonce, ciphertext);
        if (!isEqualConstantTime(calculatedMac, mac)) {
            throw new SecurityException("Invalid MAC: Message authentication failed");
        }

        byte[] padded = ChaCha20.decrypt(chachaKey, chachaNonce, ciphertext);
        byte[] raw = unpad(padded);
        return new String(raw, StandardCharsets.UTF_8);
    }

    private static byte[] randomNonce() {
        return MoreRandom.randomByteArray(NONCE_SIZE);
    }

    private static byte[][] decodePayload(String payload) {
        int payloadLength = payload.length();
        if (payloadLength < PAYLOAD_MIN_SIZE || payloadLength > PAYLOAD_MAX_SIZE) {
            throw new IllegalArgumentException("Invalid payload length: %d".formatted(payloadLength));
        }
        if (payload.charAt(0) == NON_BASE64_FLAG) {
            throw new IllegalArgumentException("Unknown encryption version");
        }

        byte[] data = Base64.getDecoder().decode(payload);
        if (data.length < DATA_MIN_SIZE || data.length > DATA_MAX_SIZE) {
            throw new IllegalArgumentException("Invalid data length: %d".formatted(data.length));
        }
        if (data[0] != VERSION_V2) {
            throw new IllegalArgumentException("Unknown encryption version: %d".formatted(data[0]));
        }

        byte[] nonce = Arrays.copyOfRange(data, VERSION_SIZE, VERSION_SIZE + NONCE_SIZE);
        byte[] ciphertext = Arrays.copyOfRange(data, VERSION_SIZE + NONCE_SIZE, data.length - MAC_SIZE);
        byte[] mac = Arrays.copyOfRange(data, data.length - MAC_SIZE, data.length);

        return new byte[][]{nonce, ciphertext, mac};
    }

    /**
     * Calculates unique per-message key.
     * <p>
     * Slice 76-byte HKDF output into: chacha_key (bytes 0..32), chacha_nonce (bytes 32..44), hmac_key (bytes 44..76)
     */
    static byte[][] toMessageKeys(PrivateKey conversationKey, byte[] nonce) {
        if (nonce.length != NONCE_SIZE) {
            throw new IllegalArgumentException("Invalid nonce: Must be 32 bytes, got %d.".formatted(nonce.length));
        }
        byte[] keys = Hkdf.expand(conversationKey, nonce, HKDF_EXPAND_LENGTH);
        byte[] chachaKey = Arrays.copyOfRange(keys, 0, 32);
        byte[] chachaNonce = Arrays.copyOfRange(keys, 32, 44);
        byte[] hmacKey = Arrays.copyOfRange(keys, 44, HKDF_EXPAND_LENGTH);
        return new byte[][]{chachaKey, chachaNonce, hmacKey};
    }

    /**
     * Calculates length of the padded byte array.
     */
    static int calcPaddedLength(int length) {
        if (length <= 32) {
            return 32;
        }

        int nextPower = 1 << (32 - Integer.numberOfLeadingZeros(length - 1));
        int chunk = nextPower <= 256 ? 32 : nextPower / 8;
        return chunk * (((length - 1) / chunk) + 1);
    }

    /**
     * Converts unpadded plaintext to padded byte array.
     */
    private static byte[] pad(byte[] raw) {
        byte[] prefix = writeU16Be(raw.length);
        byte[] suffix = zeroes(calcPaddedLength(raw.length) - raw.length);
        return concat(prefix, raw, suffix);
    }

    private static byte[] unpad(byte[] padded) {
        if (padded.length < 3) {
            throw new IllegalArgumentException("Invalid padding");
        }

        int rawLength = (padded[0] & 0xff) << 8 | (padded[1] & 0xff);
        byte[] raw = Arrays.copyOfRange(padded, 2, rawLength + 2);

        if (raw.length != rawLength
            || rawLength < PLAINTEXT_MIN_SIZE
            || rawLength > PLAINTEXT_MAX_SIZE
            || padded.length != 2 + calcPaddedLength(rawLength)) {
            throw new IllegalArgumentException("Invalid padding");
        }
        return raw;
    }

    private static byte[] zeroes(int length) {
        return new byte[length];
    }

    private static byte[] writeU16Be(int val) {
        return new byte[]{
                Integer.valueOf((val >> 8) & 0xff).byteValue(),
                Integer.valueOf(val & 0xff).byteValue()
        };
    }

    private static byte[] concat(byte[]... data) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (byte[] bytes : data) {
                outputStream.write(bytes);
            }
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * A constant-time equality check of 2 byte arrays.
     *
     * @param a first array
     * @param b second array
     * @return equality of given arrays
     */
    private static boolean isEqualConstantTime(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    private static final class MoreRandom {
        private static final SecureRandom RANDOM = new SecureRandom();

        private MoreRandom() {
            throw new UnsupportedOperationException();
        }

        static byte[] randomByteArray(int length) {
            byte[] bytes = new byte[length];
            RANDOM.nextBytes(bytes);
            return bytes;
        }
    }
}
