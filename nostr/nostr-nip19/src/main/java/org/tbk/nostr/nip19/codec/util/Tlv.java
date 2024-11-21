package org.tbk.nostr.nip19.codec.util;

import lombok.Builder;
import lombok.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class Tlv {
    private Tlv() {
        throw new UnsupportedOperationException();
    }

    public static byte[] encode(List<Entry> entries) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        entries.forEach(it -> outputStream.writeBytes(it.toByteArray()));
        return outputStream.toByteArray();
    }

    public static List<Entry> decode(byte[] raw) {
        List<Entry> entries = new LinkedList<>();

        ByteArrayInputStream in = new ByteArrayInputStream(raw);

        while (true) {
            int type = in.read();
            if (type == -1) {
                break;
            }
            int length = in.read();
            if (length == -1) {
                break;
            }

            try {
                byte[] value = in.readNBytes(length);
                if (value.length != length) {
                    break;
                }

                entries.add(Entry.builder()
                        .type((byte) type)
                        .value(value)
                        .build());

            } catch (IOException e) {
                break;
            }
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
