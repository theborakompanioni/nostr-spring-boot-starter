package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.stream.IntStream;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventId implements Comparable<EventId> {

    public static EventId fromHex(String id) {
        return of(HexFormat.of().parseHex(id));
    }

    public static EventId of(byte[] id) {
        return new EventId(id);
    }

    @EqualsAndHashCode.Include
    private final byte[] raw;

    private EventId(byte[] raw) {
        if (raw.length != 32) {
            throw new IllegalArgumentException("Event id must have length of 32 bytes");
        }

        this.raw = Arrays.copyOf(raw, raw.length);
    }

    public byte[] toByteArray() {
        return Arrays.copyOf(raw, raw.length);
    }

    public String toHex() {
        return HexFormat.of().formatHex(raw);
    }

    @Override
    public int compareTo(EventId o) {
        return UNSIGNED_LEXICOGRAPHICAL_COMPARATOR.compare(this.raw, o.raw);
    }

    @Override
    public String toString() {
        return "EventId[%s]".formatted(this.toHex());
    }

    private static final int UNSIGNED_BYTE_MASK = 0xFF;

    /**
     * Returns the value of the given byte as an integer, interpreting the byte as an unsigned value.
     * That is, returns {@code value + 256} if {@code value} is negative; {@code value} itself
     * otherwise.
     *
     * <p>Note: This code was copied from {@link com.google.protobuf.ByteString#toInt}.
     */
    private static int toInt(byte value) {
        return value & UNSIGNED_BYTE_MASK;
    }

    /**
     * Compares two {@link EventId}s lexicographically, treating their contents as unsigned byte
     * values between 0 and 255 (inclusive).
     *
     * <p>For example, {@code (byte) -1} is considered to be greater than {@code (byte) 1} because it
     * is interpreted as an unsigned value, {@code 255}.
     *
     * <p>Note: This code was copied from {@link com.google.protobuf.ByteString#unsignedLexicographicalComparator()}.
     */
    private static final Comparator<byte[]> UNSIGNED_LEXICOGRAPHICAL_COMPARATOR = (former, latter) -> {
        Iterator<Byte> formerBytes = IntStream.range(0, former.length).mapToObj(it -> former[it]).iterator();
        Iterator<Byte> latterBytes = IntStream.range(0, latter.length).mapToObj(it -> latter[it]).iterator();

        while (formerBytes.hasNext() && latterBytes.hasNext()) {
            int result = Integer.compare(toInt(formerBytes.next()), toInt(latterBytes.next()));
            if (result != 0) {
                return result;
            }
        }
        return Integer.compare(former.length, latter.length);
    };
}
