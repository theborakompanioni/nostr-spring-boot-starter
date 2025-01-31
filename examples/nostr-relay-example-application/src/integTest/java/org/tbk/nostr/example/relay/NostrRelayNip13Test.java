package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.relay.config.nip13.Nip13Properties;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip13-test"})
class NostrRelayNip13Test {

    @Autowired
    private Nip13Properties nip13ExtensionProperties;

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void contextLoads() {
        assertThat(nip13ExtensionProperties, is(notNullValue()));
        assertThat(nip13ExtensionProperties.getMinPowDifficulty(), is(8));
    }

    @Test
    void itShouldAcceptEventMeetingTargetDifficulty() {
        Signer signer = SimpleSigner.random();

        Event.Builder eventBuilder = Nip13.mineEvent(
                Nip1.createTextNote(signer.getPublicKey(), "GM"),
                nip13ExtensionProperties.getMinPowDifficulty()
        );

        Event event = MoreEvents.finalize(signer, eventBuilder);

        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty()), is(true));

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));
    }

    @Test
    void itShouldDeclineEventNotMeetingTargetDifficulty() {
        Signer signer = SimpleSigner.random();

        assertThat("sanity check", nip13ExtensionProperties.getMinPowDifficulty(), is(greaterThan(0)));

        Event event = requireNonNull(Flux.fromStream(Stream.generate(() -> MoreEvents.createFinalizedTextNote(signer, MoreSubscriptionIds.random().getId()))
                        .filter(it -> !Nip13.meetsTargetDifficulty(it, nip13ExtensionProperties.getMinPowDifficulty(), false)))
                .blockFirst(Duration.ofSeconds(10)));

        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), false), is(false));
        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), true), is(false));

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(false));

        String expectedMessage = "pow: Difficulty %d is less than %d.".formatted(
                Nip13.calculateDifficulty(event),
                nip13ExtensionProperties.getMinPowDifficulty()
        );
        assertThat(ok.getMessage(), is(expectedMessage));
    }

    @Test
    void itShouldDeclineEventMeetingTargetButDoesNotCommitToDifficulty() {
        Signer signer = SimpleSigner.random();

        Event.Builder eventBuilder = Nip13.mineEvent(
                Nip1.createTextNote(signer.getPublicKey(), "GM"),
                nip13ExtensionProperties.getMinPowDifficulty(),
                Nip13.nonceWithoutCommitment(0).toBuilder()
        );

        Event event = MoreEvents.finalize(signer, eventBuilder);

        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), false), is(true));
        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), true), is(false));

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is("pow: Missing or invalid pow commitment."));
        assertThat(ok.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineEventMeetingTargetButCommitsToWrongDifficulty() {
        Signer signer = SimpleSigner.random();

        Event.Builder eventBuilder = Nip13.mineEvent(
                Nip1.createTextNote(signer.getPublicKey(), "GM"),
                nip13ExtensionProperties.getMinPowDifficulty(),
                Nip13.nonce(0, nip13ExtensionProperties.getMinPowDifficulty() + 1).toBuilder()
        );

        Event event = MoreEvents.finalize(signer, eventBuilder);

        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), false), is(true));
        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty(), true), is(false));

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is("pow: Missing or invalid pow commitment."));
        assertThat(ok.getSuccess(), is(false));
    }
}
