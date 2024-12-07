package org.tbk.nostr.nips;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class Nip38Test {

    @Test
    void status() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip38.status(signer.getPublicKey(), "status", "Sign up for nostrasia!"));

        assertThat(event.getKind(), is(Kinds.kindUserStatuses.getValue()));
        assertThat(event.getContent(), is("Sign up for nostrasia!"));

        List<TagValue> dTags = MoreTags.findByName(event, IndexedTag.d);
        assertThat(dTags, hasSize(1));
        assertThat(dTags.getLast().getValues(0), is("status"));

        assertThat(Nip40.getExpiration(event).isPresent(), is(false));
    }

    @Test
    void general() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip38.general(signer.getPublicKey(), "Sign up for nostrasia!"));

        assertThat(event.getKind(), is(Kinds.kindUserStatuses.getValue()));
        assertThat(event.getContent(), is("Sign up for nostrasia!"));

        List<TagValue> dTags = MoreTags.findByName(event, IndexedTag.d);
        assertThat(dTags, hasSize(1));
        assertThat(dTags.getLast().getValues(0), is("general"));

        assertThat(Nip40.getExpiration(event).isPresent(), is(false));
    }

    @Test
    void emoji() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip38.general(
                signer.getPublicKey(),
                Nip30.emoji("emoji", URI.create("https://example.org/emoji.png")),
                Nip40.expiration(Instant.now())
        ));

        assertThat(event.getKind(), is(Kinds.kindUserStatuses.getValue()));
        assertThat(event.getContent(), is(Nip30.placeholder("emoji")));

        List<TagValue> dTags = MoreTags.findByName(event, IndexedTag.d);
        assertThat(dTags, hasSize(1));
        assertThat(dTags.getLast().getValues(0), is("general"));

        assertThat(Nip40.getExpiration(event).isPresent(), is(true));
    }

    @Test
    void music() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip38.music(
                signer.getPublicKey(),
                URI.create("spotify:search:Intergalatic%20-%20Beastie%20Boys"),
                "Intergalatic - Beastie Boys",
                Nip40.expiration(Duration.ofMinutes(4).plusSeconds(35))
        ));

        assertThat(event.getKind(), is(Kinds.kindUserStatuses.getValue()));
        assertThat(event.getContent(), is("Intergalatic - Beastie Boys"));

        List<TagValue> dTags = MoreTags.findByName(event, IndexedTag.d);
        assertThat(dTags, hasSize(1));
        assertThat(dTags.getLast().getValues(0), is("music"));

        List<TagValue> rTags = MoreTags.findByName(event, IndexedTag.r);
        assertThat(rTags, hasSize(1));
        assertThat(rTags.getLast().getValues(0), is("spotify:search:Intergalatic%20-%20Beastie%20Boys"));

        assertThat(Nip40.getExpiration(event).isPresent(), is(true));
    }
}
