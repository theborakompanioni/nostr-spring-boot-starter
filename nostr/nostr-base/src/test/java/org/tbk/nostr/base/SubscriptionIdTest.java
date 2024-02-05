package org.tbk.nostr.base;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionIdTest {

    @Test
    void itShouldCreateSubscriptionId() {
        assertThat(SubscriptionId.of("a").getId(), is("a"));
        assertThat(SubscriptionId.of("a".repeat(64)).getId(), is("a".repeat(64)));
    }

    @Test
    void itShouldFailToCreateInvalidSubscriptionIds() {
        assertThrows(NullPointerException.class, () -> {
            SubscriptionId.of(null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            SubscriptionId.of("");
        }, "SubscriptionId must have between 1 and 64 chars");
        assertThrows(IllegalArgumentException.class, () -> {
            SubscriptionId.of("1".repeat(65));
        }, "SubscriptionId must have between 1 and 64 chars");
    }
}
