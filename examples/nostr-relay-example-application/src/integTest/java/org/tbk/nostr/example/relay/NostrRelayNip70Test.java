package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip42;
import org.tbk.nostr.nips.Nip70;
import org.tbk.nostr.proto.AuthResponse;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip42-test"})
class NostrRelayNip70Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Autowired
    private NostrClientService nostrClient;

    @BeforeEach
    public void beforeEach() {
        // force new connection so successful authentications are forgotten and test order is not relevant
        nostrClient.reconnect(Duration.ofSeconds(0)).subscribe();
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(nostrClient.isConnected(), is(true));
        });
    }

    @Test
    void itShouldDeclineProtectedEventIfUserIsNotAuthenticated() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip70.protect(Nip1.createTextNote(signer.getPublicKey(), "GM")));

        OkResponse ok0 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("auth-required: authentication required."));
    }

    @Test
    void itShouldAcceptProtectedEventIfUserIAuthenticated() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip70.protect(Nip1.createTextNote(signer.getPublicKey(), "GM")));

        AuthResponse auth0 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    // force an AUTH response
                    nostrClient.send(event0).subscribe().dispose();
                })
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .blockFirst(Duration.ofSeconds(5)));

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                auth0.getChallenge(),
                nostrClient.getRelayUri()));

        nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5));

        OkResponse ok2 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.send(event0).subscribe().dispose();
                })
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .blockFirst(Duration.ofSeconds(5)));

        assertThat(ok2.getEventId(), is(event0.getId()));
        assertThat(ok2.getSuccess(), is(true));
        assertThat(ok2.getMessage(), is(""));
    }
}
