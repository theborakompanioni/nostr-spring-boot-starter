package org.tbk.nostr.util;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Kind;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MoreKindsTest {

    @Test
    void isReplaceable() {
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindUserMetadata().getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindShortTextNote().getValue()), is(false));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindFollows().getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindReplaceableRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindReplaceableRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isReplaceable(Kind.maxValue()), is(false));
    }

    @Test
    void isEphemeral() {
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindUserMetadata().getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindShortTextNote().getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindFollows().getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindEphemeralRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindEphemeralRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(Kind.maxValue()), is(false));
    }

    @Test
    void isParameterizedReplaceable() {
        assertThat(MoreKinds.isParameterizedReplaceable(MoreKinds.kindUserMetadata().getValue()), is(false));
        assertThat(MoreKinds.isParameterizedReplaceable(MoreKinds.kindShortTextNote().getValue()), is(false));
        assertThat(MoreKinds.isParameterizedReplaceable(MoreKinds.kindFollows().getValue()), is(false));
        assertThat(MoreKinds.isParameterizedReplaceable(MoreKinds.kindParameterizedReplaceableRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isParameterizedReplaceable(MoreKinds.kindParameterizedReplaceableRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isParameterizedReplaceable(Kind.maxValue()), is(false));
    }
}