package org.tbk.nostr.relay.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MorePublicKeys;

import java.net.URI;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "nip-test"})
public class NostrRelayNip11Test {

    @LocalServerPort
    private int serverPort;

    private NostrTemplate nostrTemplate;

    @BeforeEach
    void beforeEach() {
        if (this.nostrTemplate == null) {
            this.nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));
        }
    }

    @Test
    void itShouldFetchRelayInfoSuccessfully0() {
        RelayInfoDocument relayInfo = nostrTemplate.fetchRelayInfoDocument()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(relayInfo, is(notNullValue()));
        assertThat(relayInfo.getName(), is("tbk-nostr-relay-nip-test"));
        assertThat(relayInfo.getDescription(), is("The Times 03/Jan/2009 Chancellor on brink of second bailout for banks"));
        assertThat(relayInfo.getPubkey(), is(MorePublicKeys.fromHex("0000000000000000000000000000000000000000000000000000000000000001")));
        assertThat(relayInfo.getSoftware(), is(URI.create("https://github.com/theborakompanioni/nostr-spring-boot-starter")));
        assertThat(relayInfo.getVersion(), is("2.43.0"));
        assertThat(relayInfo.getSupportedNips(), hasItems(1, 42, 21000000));
        assertThat(relayInfo.getContact(), is(nullValue()));
    }
}
