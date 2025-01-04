package org.tbk.nostr.nips;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip9Test {

    @Test
    void itShouldCreateDeletionEventForEvent0() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM");

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEventForEvent(event0));

        TagValue eTag = MoreTags.findByNameSingle(deletionEvent0, IndexedTag.e)
                .orElseThrow(() -> new IllegalStateException("Expected an `e` tag"));

        assertThat(eTag.getValues(0), is(EventId.of(event0).toHex()));
    }

    @Test
    void itShouldCreateDeletionEventForEvent1ReplaceableEvent() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createReplaceableEvent(signer.getPublicKey(), "GM"));
        assertThat("sanity check", Nip1.isReplaceableEvent(event0));

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEventForEvent(event0));

        TagValue aTag = MoreTags.findByNameSingle(deletionEvent0, IndexedTag.a)
                .orElseThrow(() -> new IllegalStateException("Expected an `a` tag"));

        String expectedTagValue = "%d:%s".formatted(event0.getKind(), HexFormat.of().formatHex(event0.getPubkey().toByteArray()));
        assertThat(aTag.getValues(0), is(expectedTagValue));
    }

    @Test
    void itShouldCreateDeletionEventForEvent2AddressableEvent() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createAddressableEvent(signer.getPublicKey(), "GM", "test"));
        assertThat("sanity check", Nip1.isAddressableEvent(event0));

        TagValue dTag = MoreTags.findByNameSingle(event0, IndexedTag.d)
                .orElseThrow(() -> new IllegalStateException("Expected an `d` tag"));

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEventForEvent(event0));

        TagValue aTag = MoreTags.findByNameSingle(deletionEvent0, IndexedTag.a)
                .orElseThrow(() -> new IllegalStateException("Expected an `a` tag"));

        String expectedTagValue = "%d:%s:%s".formatted(event0.getKind(), signer.getPublicKey().value.toHex(), dTag.getValues(0));
        assertThat(aTag.getValues(0), is(expectedTagValue));
    }
}
