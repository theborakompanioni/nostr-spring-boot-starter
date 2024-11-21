package org.tbk.nostr.nips;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.fail;

class Nip18Test {

    @Test
    void repostShortTextNote0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM!");
        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(), event).build();

        assertThat(repost.getKind(), is(6));

        TagValue eTag = MoreTags.findByNameSingle(repost, IndexedTag.e).orElseThrow();
        assertThat(eTag.getValues(0), is(HexFormat.of().formatHex(event.getId().toByteArray())));
        assertThat(eTag.getValues(1), is(""));
    }

    @Test
    void repostShortTextNote1() {
        Signer signer = SimpleSigner.random();
        RelayUri relayUri = RelayUri.fromString("ws://localhost:%d".formatted(8080));

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM!");
        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(), event, relayUri).build();

        assertThat(repost.getKind(), is(6));

        TagValue eTag = MoreTags.findByNameSingle(repost, IndexedTag.e).orElseThrow();
        assertThat(eTag.getValues(0), is(HexFormat.of().formatHex(event.getId().toByteArray())));
        assertThat(eTag.getValues(1), is(relayUri.getUri().toString()));
    }

    @Test
    void repostShortTextNoteFail() {
        Signer signer = SimpleSigner.random();
        RelayUri relayUri = RelayUri.fromString("ws://localhost:%d".formatted(8080));

        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(),
                        MoreEvents.createFinalizedTextNote(signer, "GM!"),
                        relayUri)
                .build();

        try {
            Nip18.repostShortTextNote(signer.getPublicKey(), repost, relayUri).build();

            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Can only repost short text notes. Expected kind 1, but got 6."));
        }
    }

    @Test
    void repostGenericEvent() {
        Signer signer = SimpleSigner.random();

        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(),
                        MoreEvents.createFinalizedTextNote(signer, "GM!"),
                        RelayUri.fromString("ws://localhost:%d".formatted(8080)))
                .build();
        Event genericRepost = Nip18.repostGenericEvent(signer.getPublicKey(),
                        repost,
                        RelayUri.fromString("ws://localhost:%d".formatted(8080)))
                .build();

        assertThat(genericRepost.getKind(), is(16));
    }

    @Test
    void repostGenericEventFail() {
        Signer signer = SimpleSigner.random();
        RelayUri relayUri = RelayUri.fromString("ws://localhost:%d".formatted(8080));

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM!");

        try {
            Nip18.repostGenericEvent(signer.getPublicKey(), event, relayUri).build();

            fail("Should have thrown exception");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage(), is("Can only repost events other than short text notes. Expected kind != 1, but got 1."));
        }
    }
}