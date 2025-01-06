package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip40;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip-test"})
class NostrRelayNip40Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldSendExpiringEventSuccessfully0AlreadyExpired() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();
        Instant expiresAt = now.minusSeconds(1);
        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .addTags(MoreTags.expiration(expiresAt)));

        assertThat("sanity check", Nip40.findExpiration(event0).orElseThrow().instant(), is(expiresAt.truncatedTo(ChronoUnit.SECONDS)));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getSuccess(), is(true));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(event0))
                .blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent0.isPresent(), is(false));
    }

    @Test
    void itShouldSendExpiringEventSuccessfully1() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();
        Duration expiresIn = Duration.ofSeconds(5);
        Instant expiresAt = now.plusMillis(expiresIn.toMillis());

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .addTags(MoreTags.expiration(expiresAt)));

        assertThat("sanity check", Nip40.findExpiration(event0).orElseThrow().instant(), is(expiresAt.truncatedTo(ChronoUnit.SECONDS)));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getSuccess(), is(true));

        EventId event0Id = EventId.of(event0);
        Event fetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat("event is still present", fetchedEvent0, is(event0));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .delaySubscription(expiresIn)
                .blockOptional(expiresIn.plus(Duration.ofSeconds(5)));

        assertThat("event expired", refetchedEvent0.isPresent(), is(false));
    }
}
