package org.tbk.nostr.relay.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "sqlite-test"})
public class NostrRelaySqliteTest {

    @LocalServerPort
    private int serverPort;

    @Autowired
    private NostrRelayExampleApplicationProperties applicationProperties;

    private NostrTemplate nostrTemplate;

    @BeforeEach
    void beforeEach() {
        if (this.nostrTemplate == null) {
            this.nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));
        }
    }

    @Test
    void itShouldLoadProperties() {
        assertThat(applicationProperties.getAsyncExecutor().getMaxPoolSize(), is(1));
    }

    @Test
    void itShouldFetchEventByIdSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event eventMatching = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event eventNonMatching = MoreEvents.createFinalizedTextNote(signer, "GM1");

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Event fetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(eventMatching.getId().toByteArray()))
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(eventMatching));
        assertThat(fetchedEvent0, is(eventMatching));
    }
}
