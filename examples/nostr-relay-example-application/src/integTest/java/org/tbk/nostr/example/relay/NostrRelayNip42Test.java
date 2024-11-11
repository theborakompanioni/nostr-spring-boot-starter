package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip42-test"})
class NostrRelayNip42Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldReceiveAuthChallenge() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        List<Response> responses = nostrTemplate.sendAndCollect(event)
                .bufferTimeout(2, Duration.ofSeconds(3))
                .next()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        AuthResponse auth0 = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        OkResponse ok = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), startsWith("auth-required:"));
    }

    @Test
    void itShouldShouldDeclineEventRequestForUnauthenticated() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), startsWith("auth-required:"));
    }

    @Test
    void itShouldShouldDeclineReqRequestForUnauthenticated() {
        String subscriptionId = MoreSubscriptionIds.random().getId();
        List<Response> responses = nostrTemplate.fetch(ReqRequest.newBuilder()
                        .setId(subscriptionId)
                        .addFilters(Filter.newBuilder()
                                .addKinds(1)
                                .build())
                        .build())
                .bufferTimeout(2, Duration.ofSeconds(3))
                .next()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        AuthResponse auth0 = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        ClosedResponse closed = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.CLOSED)
                .map(Response::getClosed)
                .findFirst()
                .orElseThrow();

        assertThat(closed.getSubscriptionId(), is(subscriptionId));
        assertThat(closed.getMessage(), startsWith("auth-required:"));
    }
}
