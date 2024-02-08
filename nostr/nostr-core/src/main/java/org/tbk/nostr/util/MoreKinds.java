package org.tbk.nostr.util;

public final class MoreKinds {
    private static final int MIN_KIND_VALUE = 0;
    private static final int MAX_KIND_VALUE = 65_535;

    private MoreKinds() {
        throw new UnsupportedOperationException();
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
}
