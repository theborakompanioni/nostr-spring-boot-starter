package org.tbk.nostr.base;


import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class Kind {

    private static final int MIN_KIND_VALUE = 0;
    private static final int MAX_KIND_VALUE = 65_535;

    public static boolean isValidKindString(String kindString) {
        try {
            return isValidKind(Integer.parseInt(kindString));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidKind(int kind) {
        return kind >= MIN_KIND_VALUE && kind <= MAX_KIND_VALUE;
    }

    public static int minValue() {
        return MIN_KIND_VALUE;
    }

    public static int maxValue() {
        return MAX_KIND_VALUE;
    }

    public static Kind min() {
        return Kind.of(MIN_KIND_VALUE);
    }

    public static Kind max() {
        return Kind.of(MAX_KIND_VALUE);
    }

    public static Kind fromString(String kindString) {
        if (!isValidKindString(kindString)) {
            throw new IllegalArgumentException("Kind must be between %d and %d".formatted(MIN_KIND_VALUE, MAX_KIND_VALUE));
        }
        return of(Integer.parseInt(kindString));
    }

    public static Kind of(int kind) {
        return new Kind(kind);
    }

    @EqualsAndHashCode.Include
    @ToString.Include
    private final int value;

    private Kind(int value) {
        if (!isValidKind(value)) {
            throw new IllegalArgumentException("Kind must be between %d and %d".formatted(MIN_KIND_VALUE, MAX_KIND_VALUE));
        }

        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
