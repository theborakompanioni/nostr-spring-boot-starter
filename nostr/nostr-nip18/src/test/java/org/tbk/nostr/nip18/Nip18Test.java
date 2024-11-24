package org.tbk.nostr.nip18;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nip19.Nevent;
import org.tbk.nostr.nip19.Npub;
import org.tbk.nostr.nip21.NostrUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class Nip18Test {
    private static final RelayUri dummyRelayUri = RelayUri.parse("ws://localhost:%d".formatted(8080));

    @Test
    void repostShortTextNote0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(), event, dummyRelayUri).build();

        assertThat(repost.getKind(), is(6));

        TagValue eTag = MoreTags.findByNameSingle(repost, IndexedTag.e).orElseThrow();
        assertThat(eTag.getValues(0), is(HexFormat.of().formatHex(event.getId().toByteArray())));
        assertThat(eTag.getValues(1), is(dummyRelayUri.getUri().toString()));
    }

    @Test
    void repostShortTextNoteFail() {
        Signer signer = SimpleSigner.random();

        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(),
                        MoreEvents.createFinalizedTextNote(signer, "GM"),
                        dummyRelayUri)
                .build();

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> Nip18.repostShortTextNote(signer.getPublicKey(), repost, dummyRelayUri).build());
        assertThat(e.getMessage(), is("Can only repost short text notes. Expected kind 1, but got 6."));
    }

    @Test
    void quoteNote0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event quote = Nip18.quote(signer.getPublicKey(), event, dummyRelayUri, "GM1").build();

        assertThat(quote.getKind(), is(1));

        TagValue qTag = MoreTags.findByNameSingle(quote, IndexedTag.q).orElseThrow();
        assertThat(qTag.getValues(0), is(HexFormat.of().formatHex(event.getId().toByteArray())));
        assertThat(qTag.getValues(1), is(dummyRelayUri.getUri().toString()));
        assertThat(qTag.getValues(2), is(HexFormat.of().formatHex(event.getPubkey().toByteArray())));

        assertThat(quote.getContent(), containsString(NostrUri.of(Nevent.builder()
                .eventId(EventId.of(event.getId().toByteArray()))
                .relay(dummyRelayUri)
                .build()).getUri().toString()));
    }

    @Test
    void quoteNoteFail() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM0");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> Nip18.quote(signer.getPublicKey(), event, dummyRelayUri, "GM1", Npub.builder()
                .publicKey(MorePublicKeys.fromEvent(event))
                .build()).build());
        assertThat(e.getMessage(), is("Can only add nevent, naddr or note, got: NPUB."));
    }

    @Test
    void repostGenericEvent() {
        Signer signer = SimpleSigner.random();

        Event repost = Nip18.repostShortTextNote(signer.getPublicKey(),
                        MoreEvents.createFinalizedTextNote(signer, "GM"),
                        dummyRelayUri)
                .build();
        Event genericRepost = Nip18.repostGenericEvent(signer.getPublicKey(), repost, dummyRelayUri)
                .build();

        assertThat(genericRepost.getKind(), is(16));
    }

    @Test
    void repostGenericEventFail() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        Exception e = Assertions.assertThrows(IllegalArgumentException.class, () -> Nip18.repostGenericEvent(signer.getPublicKey(), event, dummyRelayUri).build());
        assertThat(e.getMessage(), is("Can only repost events other than short text notes. Expected kind != 1, but got 1."));
    }
}