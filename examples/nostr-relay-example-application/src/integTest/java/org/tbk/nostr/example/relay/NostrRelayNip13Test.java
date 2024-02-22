package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.relay.config.nip13.Nip13Properties;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "nip13-test"})
public class NostrRelayNip13Test {

    @LocalServerPort
    private int serverPort;

    @Autowired(required = false)
    private Nip13Properties nip13ExtensionProperties;

    private NostrTemplate nostrTemplate;

    @BeforeEach
    void beforeEach() {
        if (this.nostrTemplate == null) {
            this.nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));
        }
    }

    @Test
    void contextLoads() {
        assertThat(nip13ExtensionProperties, is(notNullValue()));
        assertThat(nip13ExtensionProperties.getMinPowDifficulty(), is(8));
    }

    @Test
    void itShouldPublishEventMeetingTargetDifficulty0() {
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
        assertThat(ok.getSuccess(), is(true));
        assertThat(ok.getMessage(), is(""));
    }

    @Test
    void itShouldDeclineEventNotMeetingTargetDifficulty0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        assertThat("sanity check", Nip13.meetsTargetDifficulty(event, nip13ExtensionProperties.getMinPowDifficulty()), is(false));

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(false));

        String expectedMessage = "Error: Difficulty %d is less than %d".formatted(
                Nip13.calculateDifficulty(event),
                nip13ExtensionProperties.getMinPowDifficulty()
        );
        assertThat(ok.getMessage(), is(expectedMessage));
    }
}
