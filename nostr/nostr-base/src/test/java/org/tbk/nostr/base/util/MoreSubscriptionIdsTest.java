package org.tbk.nostr.base.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MoreSubscriptionIdsTest {

    @Test
    void random() {
        IntStream.rangeClosed(1, 64).forEach(foo -> {
            assertThat(MoreSubscriptionIds.random().getId().length(), is(both(greaterThanOrEqualTo(32)).and(Matchers.lessThanOrEqualTo(64))));
        });
    }

    @Test
    void randomWithMinLength() {
        IntStream.rangeClosed(1, 64).forEach(minLength -> {
            assertThat(MoreSubscriptionIds.random(minLength).getId().length(), is(both(greaterThanOrEqualTo(minLength)).and(Matchers.lessThanOrEqualTo(64))));
        });
    }

    @Test
    void randomWithMinAndMaxLength() {
        IntStream.rangeClosed(1, 64).forEach(minAndMaxLength -> {
            assertThat(MoreSubscriptionIds.random(minAndMaxLength, minAndMaxLength).getId().length(), is(minAndMaxLength));
        });
    }
}
