package org.tbk.nostr.nip19.codec.util;

public class Ints {
    private Ints() {
        throw new UnsupportedOperationException();
    }

    public static byte[] toByteArray(int value) {
        return new byte[]{(byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value};
    }

    public static int fromByteArray(byte[] bytes) {
        if (bytes.length < 4) {
            throw new IllegalArgumentException("array too small: %d < %d".formatted(bytes.length, 4));
        }
        return fromBytes(bytes[0], bytes[1], bytes[2], bytes[3]);
    }

    private static int fromBytes(byte b1, byte b2, byte b3, byte b4) {
        return b1 << 24 | (b2 & 255) << 16 | (b3 & 255) << 8 | b4 & 255;
    }
}
