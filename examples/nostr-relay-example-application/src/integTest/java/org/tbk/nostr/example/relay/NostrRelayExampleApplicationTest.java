package org.tbk.nostr.example.relay;

import fr.acinq.bitcoin.PrivateKey;
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
import org.tbk.nostr.relay.config.NostrRelayProperties;
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

    @Autowired(required = false)
    private NostrRelayProperties relayProperties;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(applicationProperties, is(notNullValue()));
        assertThat(relayProperties, is(notNullValue()));
    }

    @Test
    void itShouldLoadProperties() {
        assertThat(applicationProperties.isStartupEventsEnabled(), is(true));
        assertThat(applicationProperties.getIdentity().isPresent(), is(true));
        assertThat(applicationProperties.getAsyncExecutor().getMaxPoolSize(), is(10));
        assertThat(applicationProperties.getGreeting().isPresent(), is(false));
        assertThat(applicationProperties.getInitialQueryLimit(), is(210));

        assertThat(relayProperties.getMaxLimitPerFilter(), is(2100));
        assertThat(relayProperties.getMaxFilterCount(), is(42));
    }

    @Test
    void itShouldReceiveStartupEvents() {
        NostrTemplate nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));

        XonlyPublicKey applicationPubkey = applicationProperties.getIdentity()
                .map(NostrRelayExampleApplicationProperties.IdentityProperties::getSeed)
                .map(MoreIdentities::fromSeed)
                .map(PrivateKey::xOnlyPublicKey)
                .orElseThrow();

        List<Event> events = nostrTemplate.fetchEventByAuthor(applicationPubkey)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(events.isEmpty(), is(false));
    }
}
