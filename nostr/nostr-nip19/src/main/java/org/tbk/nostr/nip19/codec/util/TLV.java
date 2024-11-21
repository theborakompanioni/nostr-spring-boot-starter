package org.tbk.nostr.nip19.codec.util;

import lombok.Builder;
import lombok.Value;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class TLV {
    private TLV() {
        throw new UnsupportedOperationException();
    }

    public static byte[] encode(List<Entry> entries) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entries.forEach(it -> outputStream.writeBytes(it.toByteArray()));
        return outputStream.toByteArray();
    }

    public static List<Entry> decode(byte[] raw) {
        int i = 0;

        List<Entry> entries = new LinkedList<>();
        while (i + 1 < raw.length) {
            int length = Byte.toUnsignedInt(raw[i + 1]);

            if (i + 2 + length > raw.length) {
                break;
            }

            byte[] value = Arrays.copyOfRange(raw, i + 2, i + 2 + length);
            entries.add(Entry.builder()
                    .type(raw[i])
                    .value(value)
                    .build());

            i = i + 2 + length;
        }

        return entries;
    }

    @Value
    @Builder
    public static class Entry {
        byte type;
        byte[] value;

        public byte[] getValue() {
            return Arrays.copyOf(this.value, this.value.length);
        }

        public byte[] toByteArray() {
            byte[] bytes = new byte[value.length + 2];
            bytes[0] = type;
            bytes[1] = Integer.valueOf(value.length).byteValue();
            System.arraycopy(value, 0, bytes, 2, value.length);
            return bytes;
        }
    }
}
