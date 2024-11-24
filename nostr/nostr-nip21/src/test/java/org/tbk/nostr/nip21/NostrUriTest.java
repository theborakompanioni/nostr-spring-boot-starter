package org.tbk.nostr.nip21;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.nip19.*;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * Test vectors taken from <a href="https://github.com/nostr-protocol/nips/blob/master/21.md#examples">NIP-21 examples</a>.
 */
class NostrUriTest {

    @Test
    void testNpub() {
        URI uri = URI.create("nostr:npub1sn0wdenkukak0d9dfczzeacvhkrgz92ak56egt7vdgzn8pv2wfqqhrjdv9");

        NostrUri nostrUri = NostrUri.of(uri);
        assertThat(nostrUri.getUri(), is(uri));

        assertThat(nostrUri.getEntity(), is(instanceOf(Npub.class)));
        assertThat(((Npub) nostrUri.getEntity()).getPublicKey().value.toHex(), is("84dee6e676e5bb67b4ad4e042cf70cbd8681155db535942fcc6a0533858a7240"));
    }

    @Test
    void testNprofile() {
        URI uri = URI.create("nostr:nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl0ne2cgukyawd24mayt8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uhkg6nzv9ejuumpv34kytnrdaksjlyr9p");

        NostrUri nostrUri = NostrUri.of(uri);
        assertThat(nostrUri.getUri(), is(uri));

        assertThat(nostrUri.getEntity(), is(instanceOf(Nprofile.class)));
        assertThat(((Nprofile) nostrUri.getEntity()).getPublicKey().value.toHex(), is("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"));
    }

    @Test
    void testNote() {
        URI uri = URI.create("nostr:note1fntxtkcy9pjwucqwa9mddn7v03wwwsu9j330jj350nvhpky2tuaspk6nqc");

        NostrUri nostrUri = NostrUri.of(uri);
        assertThat(nostrUri.getUri(), is(uri));

        assertThat(nostrUri.getEntity(), is(instanceOf(Note.class)));
        assertThat(((Note) nostrUri.getEntity()).getEventId().toHex(), is("4cd665db042864ee600ee976d6cfcc7c5ce743859462f94a347cd970d88a5f3b"));
    }

    @Test
    void testNevent() {
        URI uri = URI.create("nostr:nevent1qqstna2yrezu5wghjvswqqculvvwxsrcvu7uc0f78gan4xqhvz49d9spr3mhxue69uhkummnw3ez6un9d3shjtn4de6x2argwghx6egpr4mhxue69uhkummnw3ez6ur4vgh8wetvd3hhyer9wghxuet5nxnepm");

        NostrUri nostrUri = NostrUri.of(uri);
        assertThat(nostrUri.getUri(), is(uri));

        assertThat(nostrUri.getEntity(), is(instanceOf(Nevent.class)));
        assertThat(((Nevent) nostrUri.getEntity()).getEventId().toHex(), is("b9f5441e45ca39179320e0031cfb18e34078673dcc3d3e3a3b3a981760aa5696"));
    }

    @Test
    void testNsec() {
        URI uri = URI.create("nostr:nsec1064t9a0fxkd6mdfcwghz8ehxtwcvsfj6wp7nzlkykyeve536adeqjksgqj");

        IllegalArgumentException e = Assertions.assertThrows(IllegalArgumentException.class, () -> NostrUri.of(uri));
        assertThat(e.getMessage(), is("Unexpected value: NSEC"));
    }
}