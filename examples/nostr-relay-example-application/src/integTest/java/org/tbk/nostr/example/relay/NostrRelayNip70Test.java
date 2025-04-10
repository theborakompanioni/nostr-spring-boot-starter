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
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip70-test"})
class NostrRelayNip70Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Autowired
    private NostrClientService nostrClient;

    @BeforeEach
    public void beforeEach() {
        // force new connection so successful authentications are forgotten and test order is not relevant
        nostrClient.reconnect(Duration.ofSeconds(0)).subscribe();
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
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
        assertThat(ok0.getMessage(), is("auth-required: authentication required."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldAcceptProtectedEventIfUserIAuthenticated() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip70.protect(Nip1.createTextNote(signer.getPublicKey(), "GM")));

        List<Response> responses0 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    // force an AUTH response
                    nostrClient.send(event0).subscribe().dispose();
                })
                .bufferTimeout(2, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok0 = responses0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getMessage(), startsWith("auth-required:"));
        assertThat(ok0.getSuccess(), is(false));

        AuthResponse auth0 = responses0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                auth0.getChallenge(),
                nostrClient.getRelayUri()));

        OkResponse okAuth = nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .blockFirst(Duration.ofSeconds(5));

        assertThat(okAuth.getEventId(), is(authEvent.getId()));
        assertThat(okAuth.getMessage(), is(""));
        assertThat(okAuth.getSuccess(), is(true));

        OkResponse ok2 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.send(event0).subscribe().dispose();
                })
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .blockFirst(Duration.ofSeconds(5)));

        assertThat(ok2.getEventId(), is(event0.getId()));
        assertThat(ok2.getMessage(), is(""));
        assertThat(ok2.getSuccess(), is(true));
    }
}
