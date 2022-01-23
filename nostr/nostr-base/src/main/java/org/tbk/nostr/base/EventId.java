package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Arrays;
import java.util.HexFormat;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class EventId {

    public static EventId fromHex(String id) {
        return of(HexFormat.of().parseHex(id));
    }

    public static EventId of(byte[] id) {
        return new EventId(id);
    }

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
}
