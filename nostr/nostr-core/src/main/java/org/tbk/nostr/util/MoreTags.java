package org.tbk.nostr.util;

import org.tbk.nostr.proto.TagValue;

import java.util.Arrays;

public final class MoreTags {

    private MoreTags() {
        throw new UnsupportedOperationException();
    }

    public static TagValue e(String... values) {
        return named("e", values);
    }

    public static TagValue p(String... values) {
        return named("p", values);
    }

    public static TagValue named(String name, String... values) {
        return TagValue.newBuilder().setName(name)
                .addAllValues(Arrays.asList(values))
                .build();
    }
}
