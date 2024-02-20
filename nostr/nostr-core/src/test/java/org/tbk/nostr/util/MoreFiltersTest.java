package org.tbk.nostr.util;


import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip10;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MoreFiltersTest {
    private static final Signer testSigner0 = SimpleSigner.fromPrivateKeyHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");
    private static final Signer testSigner1 = SimpleSigner.random();

    @Test
    void itShouldNotMatchEvent0() {
        Event event0 = MoreEvents.finalize(testSigner0, Nip1.createTextNote(testSigner0.getPublicKey(), "GM0"));
        Event event1 = MoreEvents.finalize(testSigner1, Nip1.createTextNote(testSigner1.getPublicKey(), "GM1")
                .addTags(MoreTags.e(event0)));

        // id
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addIds(event1.getId())
                .build()), is(false));
        // author
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addAuthors(event1.getPubkey())
                .build()), is(false));
        // kind
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addKinds(event0.getKind() + 1)
                .build()), is(false));
        // until
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt() - 1)
                .build()), is(false));
        // since
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setSince(event0.getCreatedAt() + 1)
                .build()), is(false));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt() - 1)
                .setSince(event0.getCreatedAt() + 1)
                .build()), is(false));
        // tags
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addTags(MoreTags.named("Z", "any"))
                .build()), is(false));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.MENTION))
                .addTags(MoreTags.named("Z", "any"))
                .build()), is(false));

        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addIds(event1.getId())
                .addAuthors(event1.getPubkey())
                .setUntil(event0.getCreatedAt() - 1)
                .setSince(event0.getCreatedAt() + 1)
                .build()), is(false));
    }

    @Test
    void itShouldMatchEvent0() {
        Event event0 = MoreEvents.finalize(testSigner0, Nip1.createTextNote(testSigner0.getPublicKey(), "GM0"));
        Event event1 = MoreEvents.finalize(testSigner1, Nip1.createTextNote(testSigner1.getPublicKey(), "GM1")
                .addTags(MoreTags.e(event0)));

        assertThat(MoreFilters.matches(event0, Filter.newBuilder().build()), is(true));
        // id
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addIds(event0.getId())
                .build()), is(true));
        // author
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addAuthors(event0.getPubkey())
                .build()), is(true));
        // kind
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addKinds(event0.getKind())
                .build()), is(true));
        // until
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt())
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt() + 1)
                .build()), is(true));
        // since
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setSince(event0.getCreatedAt())
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setSince(event0.getCreatedAt() - 1)
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt())
                .setSince(event0.getCreatedAt())
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .setUntil(event0.getCreatedAt() + 1)
                .setSince(event0.getCreatedAt() - 1)
                .build()), is(true));
        // tags
        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0))
                .build()), is(true));

        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addIds(event0.getId())
                .addAuthors(event0.getPubkey())
                .setUntil(event0.getCreatedAt() + 1)
                .setSince(event0.getCreatedAt() - 1)
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addIds(event0.getId())
                .addIds(event1.getId())
                .build()), is(true));
        assertThat(MoreFilters.matches(event0, Filter.newBuilder()
                .addAuthors(event0.getPubkey())
                .addAuthors(event1.getPubkey())
                .build()), is(true));
    }

    @Test
    void itShouldMatchEvent1() {
        Event event0 = MoreEvents.finalize(testSigner0, Nip1.createTextNote(testSigner0.getPublicKey(), "GM0"));
        Event event1 = MoreEvents.finalize(testSigner1, Nip1.createTextNote(testSigner1.getPublicKey(), "GM1"));

        assertThat(MoreFilters.matches(event0, List.of(
                Filter.newBuilder()
                        .addIds(event1.getId())
                        .build(),
                Filter.newBuilder()
                        .addIds(event0.getId())
                        .build())
        ), is(true));
        assertThat(MoreFilters.matches(event0, List.of(
                Filter.newBuilder()
                        .addAuthors(event1.getPubkey())
                        .build(),
                Filter.newBuilder()
                        .addAuthors(event0.getPubkey())
                        .build())
        ), is(true));
        assertThat(MoreFilters.matches(event0, List.of(
                Filter.newBuilder()
                        .addIds(event1.getId())
                        .build(),
                Filter.newBuilder()
                        .addAuthors(event0.getPubkey())
                        .build())
        ), is(true));
        assertThat(MoreFilters.matches(event0, List.of(
                Filter.newBuilder()
                        .addIds(event1.getId())
                        .build(),
                Filter.newBuilder()
                        .addKinds(event0.getKind())
                        .build())
        ), is(true));
        assertThat(MoreFilters.matches(event0, List.of(
                Filter.newBuilder()
                        .addIds(event1.getId())
                        .build(),
                Filter.newBuilder()
                        .setUntil(event0.getCreatedAt())
                        .build())
        ), is(true));
    }

    @Test
    void itShouldMatchEvent2Tags() {
        Event event0 = MoreEvents.finalize(testSigner0, Nip1.createTextNote(testSigner0.getPublicKey(), "GM0"));
        Event event1 = MoreEvents.finalize(testSigner1, Nip1.createTextNote(testSigner1.getPublicKey(), "GM1")
                .addTags(MoreTags.e(event0))
                .addTags(MoreTags.p(testSigner0.getPublicKey())));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.REPLY))
                .build()), is(true));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.ROOT))
                .build()), is(true));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.MENTION))
                .build()), is(true));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.p(testSigner0.getPublicKey()))
                .build()), is(true));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.ROOT))
                .addTags(MoreTags.p(testSigner0.getPublicKey()))
                .build()), is(true));

        assertThat(MoreFilters.matches(event1, Filter.newBuilder()
                .addTags(MoreTags.e(event0, Nip10.Marker.MENTION))
                .addTags(MoreTags.p(testSigner0.getPublicKey()))
                .build()), is(true));
    }
}
