package org.tbk.nostr.example.relay;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.persona.Persona;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.relay.plugin.allowlist.Allowlist;
import org.tbk.nostr.relay.plugin.allowlist.config.AllowlistPluginProperties;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "plugin-allowlist-test"})
class NostrRelayPluginAllowlistTest {

    @Autowired
    private AllowlistPluginProperties allowlistPluginProperties;

    @Autowired
    private Allowlist allowlist;

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void contextLoads() {
        assertThat(allowlistPluginProperties, is(notNullValue()));
        assertThat(allowlist, is(notNullValue()));
    }

    @Test
    void itShouldVerifyAllowlistEntries() {
        List<XonlyPublicKey> allowed = allowlistPluginProperties.getAllowed();

        Stream.of(0, 1, 2)
                .map(it -> Persona.alice().deriveAccount(it))
                .forEach(it -> {
                    assertThat("alice is allowed", allowed.contains(it.getPublicKey()), is(true));
                });

        Stream.of(0, 1, 2)
                .map(it -> Persona.bob().deriveAccount(it))
                .forEach(it -> {
                    assertThat("bob is not allowed", allowed.contains(it.getPublicKey()), is(false));
                });
    }

    @Test
    void itShouldDeclineEventFromPubkeyNotInAllowlist() {
        Signer signer = SimpleSigner.random();

        assertThat("sanity check", allowlist.isAllowed(signer.getPublicKey()), is(false));

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is("blocked: pubkey is not allowed."));
        assertThat(ok.getSuccess(), is(false));
    }

    @Test
    void itShouldAllowEventFromPubkeyInAllowlist() {
        Identity.Account alice0 = Persona.alice().deriveAccount(0);
        SimpleSigner signer = SimpleSigner.fromAccount(alice0);

        assertThat("sanity check", allowlist.isAllowed(signer.getPublicKey()), is(true));

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));
    }
}
