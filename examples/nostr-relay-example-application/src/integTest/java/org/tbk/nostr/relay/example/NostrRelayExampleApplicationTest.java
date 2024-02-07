package org.tbk.nostr.relay.example;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NostrRelayExampleApplicationTest {

    @LocalServerPort
    private int serverPort;

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private NostrRelayExampleApplicationProperties applicationProperties;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(applicationProperties, is(notNullValue()));
    }

    @Test
    void itShouldReceiveStartupEvents() {
        assertThat("sanity check", applicationProperties.isStartupEventsEnabled(), is(true));

        NostrTemplate nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));

        XonlyPublicKey applicationPubkey = MoreIdentities.fromSeed(applicationProperties.getIdentity().getSeed()).xOnlyPublicKey();

        List<Event> events = nostrTemplate.fetchEventByAuthor(applicationPubkey)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(events.isEmpty(), is(false));
    }
}
