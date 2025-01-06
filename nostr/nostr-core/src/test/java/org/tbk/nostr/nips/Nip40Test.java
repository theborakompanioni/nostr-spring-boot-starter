package org.tbk.nostr.nips;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip40Test {
    private static final XonlyPublicKey testPubkey = SimpleSigner.random().getPublicKey();

    @Test
    void itShouldGetExpiration0() {
        Instant now = Instant.now();

        Instant expiresAt = now.plusSeconds(21).plusMillis(21);
        Event event = Nip1.createTextNote(testPubkey, "GM")
                .addTags(Nip40.expirationTag(expiresAt))
                .build();

        assertThat(Nip40.findExpiration(event).orElseThrow().instant(), is(expiresAt.truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    void itShouldGetExpiration1Earliest() {
        Instant now = Instant.now();

        Instant expiresAt = now.plusSeconds(21).plusMillis(21);
        Event event = Nip1.createTextNote(testPubkey, "GM")
                .addTags(Nip40.expirationTag(expiresAt.plusSeconds(42)))
                .addTags(Nip40.expirationTag(expiresAt.plusSeconds(21_000)))
                .addTags(Nip40.expirationTag(expiresAt))
                .addTags(Nip40.expirationTag(expiresAt.plusSeconds(42_00)))
                .addTags(Nip40.expirationTag(expiresAt.plusSeconds(21)))
                .build();

        assertThat(Nip40.findExpiration(event).orElseThrow().instant(), is(expiresAt.truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    void itShouldGetExpiration2Empty() {
        Event event = Nip1.createTextNote(testPubkey, "GM")
                .addTags(MoreTags.named("alt", "test"))
                .build();
        assertThat(Nip40.findExpiration(event), is(Optional.empty()));
    }
}
