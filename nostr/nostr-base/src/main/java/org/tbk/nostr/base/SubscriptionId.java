package org.tbk.nostr.base;


import lombok.EqualsAndHashCode;
import lombok.ToString;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class SubscriptionId {

    public static SubscriptionId of(String id) {
        return new SubscriptionId(id);
    }

    @EqualsAndHashCode.Include
    @ToString.Include
    private final String id;

    private SubscriptionId(String id) {
        requireNonNull(id);

        if (id.isEmpty() || id.length() > 64) {
            throw new IllegalArgumentException("SubscriptionId must have between 1 and 64 chars");
        }

        this.id = id;
    }

    public String getId() {
        return id;
    }
}
