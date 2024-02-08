package org.tbk.nostr.util;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.SubscriptionId;

import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MoreSubscriptionIdsTest {

    @RepeatedTest(21)
    void random() {
        SubscriptionId random0 = MoreSubscriptionIds.random();
        assertThat(random0.getId().length(), is(both(greaterThanOrEqualTo(32)).and(Matchers.lessThanOrEqualTo(64))));

        SubscriptionId random1 = MoreSubscriptionIds.random();
        assertThat(random1.getId().length(), is(both(greaterThanOrEqualTo(32)).and(Matchers.lessThanOrEqualTo(64))));

        assertThat(random0, not(is(random1)));
    }

    @Test
    void randomFail() {
        assertThrows(IllegalArgumentException.class, () -> {
            MoreSubscriptionIds.random(0);
        }, "minLength must be greater than or equal to 1");
        assertThrows(IllegalArgumentException.class, () -> {
            MoreSubscriptionIds.random(1, 65);
        }, "maxLength must be lower than or equal to 64");
        assertThrows(IllegalArgumentException.class, () -> {
            MoreSubscriptionIds.random(42, 21);
        }, "minLength must be lower than or equal to maxLength");
    }

    @RepeatedTest(64)
    void randomWithMinLength(RepetitionInfo info) {
        IntStream.rangeClosed(info.getCurrentRepetition(), 64).forEach(minLength -> {
            SubscriptionId random0 = MoreSubscriptionIds.random(minLength);
            assertThat(random0.getId().length(), is(both(greaterThanOrEqualTo(minLength)).and(Matchers.lessThanOrEqualTo(64))));
        });
    }

    @RepeatedTest(64)
    void randomWithMinAndMaxLength(RepetitionInfo info) {
        IntStream.rangeClosed(info.getCurrentRepetition(), 64).forEach(minAndMaxLength -> {
            SubscriptionId random0 = MoreSubscriptionIds.random(minAndMaxLength, minAndMaxLength);
            assertThat(random0.getId().length(), is(minAndMaxLength));
        });
    }
}
