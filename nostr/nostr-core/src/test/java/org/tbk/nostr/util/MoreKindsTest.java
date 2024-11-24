package org.tbk.nostr.util;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.Kinds;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MoreKindsTest {

    @Test
    void isReplaceable() {
        assertThat(MoreKinds.isReplaceable(Kinds.kindProfileMetadata.getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(Kinds.kindTextNote.getValue()), is(false));
        assertThat(MoreKinds.isReplaceable(Kinds.kindFollowList.getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindReplaceableRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isReplaceable(MoreKinds.kindReplaceableRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isReplaceable(Kind.maxValue()), is(false));
    }

    @Test
    void isEphemeral() {
        assertThat(MoreKinds.isEphemeral(Kinds.kindProfileMetadata.getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(Kinds.kindTextNote.getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(Kinds.kindFollowList.getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindEphemeralRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isEphemeral(MoreKinds.kindEphemeralRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isEphemeral(Kind.maxValue()), is(false));
    }

    @Test
    void isAddressable() {
        assertThat(MoreKinds.isAddressable(Kinds.kindProfileMetadata.getValue()), is(false));
        assertThat(MoreKinds.isAddressable(Kinds.kindTextNote.getValue()), is(false));
        assertThat(MoreKinds.isAddressable(Kinds.kindFollowList.getValue()), is(false));
        assertThat(MoreKinds.isAddressable(MoreKinds.kindAddressableRange().lowerEndpoint().getValue()), is(true));
        assertThat(MoreKinds.isAddressable(MoreKinds.kindAddressableRange().upperEndpoint().getValue()), is(false));
        assertThat(MoreKinds.isAddressable(Kind.maxValue()), is(false));
    }
}