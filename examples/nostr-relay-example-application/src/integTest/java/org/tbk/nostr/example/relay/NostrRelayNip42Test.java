package org.tbk.nostr.example.relay;

import lombok.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip42;
import org.tbk.nostr.nips.Nip70;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;
import org.tbk.nostr.util.MoreTags;

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
@ActiveProfiles({"test", "nip42-test"})
class NostrRelayNip42Test {

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

    @NonNull
    private static Event createProtectedEventDummy(Signer signer) {
        return MoreEvents.finalize(signer, Nip70.protect(Nip1.createTextNote(signer.getPublicKey(), "GM")));
    }

    @Test
    void itShouldReceiveAuthChallenge() {
        Signer signer = SimpleSigner.random();

        Event event = createProtectedEventDummy(signer);

        List<Response> responses = nostrTemplate.publishEvent(event)
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

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), startsWith("auth-required:"));
    }

    @Test
    void itShouldShouldDeclineEventRequestForUnauthenticated0() {
        Signer signer = SimpleSigner.random();

        Event event = createProtectedEventDummy(signer);

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), startsWith("auth-required:"));
    }

    @Test
    void itShouldShouldDeclineEventRequestForUnauthenticated1() {
        Signer signer = SimpleSigner.random();

        Event event0 = createProtectedEventDummy(signer);

        List<Response> responses = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    // force an AUTH response
                    nostrClient.send(event0).subscribe().dispose();
                })
                .bufferTimeout(2, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getEventId(), is(event0.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), startsWith("auth-required:"));

        AuthResponse auth0 = responses.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));
    }

    @Test
    @Disabled("Must inject own Nip42Support that requires authentication for REQ requests")
    void itShouldShouldDeclineReqRequestForUnauthenticated() {
        String subscriptionId = MoreSubscriptionIds.random().getId();
        List<Response> responses = nostrTemplate.fetch(ReqRequest.newBuilder()
                        .setId(subscriptionId)
                        .addFilters(Filter.newBuilder()
                                .addKinds(Kinds.kindTextNote.getValue())
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

    @Test
    void itShouldSendSameAuthChallengesMultipleTimes() {
        Signer signer = SimpleSigner.random();

        Event event0 = createProtectedEventDummy(signer);

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
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), startsWith("auth-required:"));

        AuthResponse auth0 = responses0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        List<Response> response1 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    // force an AUTH response
                    nostrClient.send(event0).subscribe().dispose();
                })
                .bufferTimeout(2, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok1 = response1.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok1, is(ok0));

        AuthResponse auth1 = response1.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth1, is(auth0));
    }

    @Test
    void itShouldVerifyErrorOnInvalidAuthEvent0NoChallengeAssociated() {
        Signer signer = SimpleSigner.random();

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                "challengestringhere",
                nostrClient.getRelayUri()));

        List<Response> response = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok = response.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getEventId(), is(authEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("error: Unknown auth challenge."));
    }

    @Test
    void itShouldVerifyErrorOnInvalidAuthEvent1UnknownChallenge() {
        Signer signer = SimpleSigner.random();

        Event event0 = createProtectedEventDummy(signer);

        List<Response> response0 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.send(event0).subscribe().dispose();
                })
                .bufferTimeout(2, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok0 = response0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), startsWith("auth-required:"));

        AuthResponse auth0 = response0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                "00" + auth0.getChallenge(),
                nostrClient.getRelayUri()));

        List<Response> response1 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok1 = response1.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok1.getEventId(), is(authEvent.getId()));
        assertThat(ok1.getSuccess(), is(false));
        assertThat(ok1.getMessage(), is("error: Unknown auth challenge."));
    }

    @Test
    void itShouldVerifyErrorOnInvalidAuthEvent2InvalidKind() {
        Signer signer = SimpleSigner.random();

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                        "challengestringhere",
                        nostrClient.getRelayUri())
                .setKind(Kinds.kindClientAuthentication.getValue() + 1));

        List<Response> response = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok = response.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getEventId(), is(authEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("invalid: Kind must be 22242"));
    }

    @Test
    void itShouldVerifyErrorOnInvalidAuthEvent2MissingTags() {
        Signer signer = SimpleSigner.random();

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                        "challengestringhere",
                        nostrClient.getRelayUri())
                .clearTags());

        List<Response> response = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok = response.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getEventId(), is(authEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("invalid: Missing 'challenge' tag."));
    }

    @Test
    void itShouldVerifyErrorOnInvalidAuthEvent3InvalidSig() {
        Signer signer = SimpleSigner.random();

        Event.Builder authEventBuilder = Nip42.createAuthEvent(signer.getPublicKey(),
                "challengestringhere",
                nostrClient.getRelayUri());
        Event verifiedEvent0 = MoreEvents.verifySignature(MoreEvents.finalize(signer, authEventBuilder));
        assertThat("sanity check", MoreEvents.hasValidSignature(verifiedEvent0), is(true));

        Event verifiedEvent1 = MoreEvents.verifySignature(MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                Nip42.getChallenge(authEventBuilder) + "!",
                nostrClient.getRelayUri())));
        assertThat("sanity check", MoreEvents.hasValidSignature(verifiedEvent1), is(true));

        assertThat("sanity check - id differs", verifiedEvent1.getId(), not(is(verifiedEvent0.getId())));
        assertThat("sanity check - sig differs", verifiedEvent1.getSig(), not(is(verifiedEvent0.getSig())));

        Event invalidEvent = verifiedEvent1.toBuilder()
                .setSig(verifiedEvent0.getSig())
                .build();

        assertThat(MoreEvents.hasValidSignature(invalidEvent), is(false));

        List<Response> response = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(invalidEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok = response.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok.getEventId(), is(invalidEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("invalid: Invalid signature."));
    }

    @Test
    void itShouldAuthenticateSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event0 = createProtectedEventDummy(signer);

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
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), startsWith("auth-required:"));

        AuthResponse auth0 = responses0.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.AUTH)
                .map(Response::getAuth)
                .findFirst()
                .orElseThrow();

        assertThat(auth0.getChallenge(), is(notNullValue()));

        Event authEvent = MoreEvents.finalize(signer, Nip42.createAuthEvent(signer.getPublicKey(),
                auth0.getChallenge(),
                nostrClient.getRelayUri()));

        List<Response> response1 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.auth(authEvent).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok1 = response1.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok1.getEventId(), is(authEvent.getId()));
        assertThat(ok1.getSuccess(), is(true));
        assertThat(ok1.getMessage(), is(""));

        List<Response> response2 = requireNonNull(nostrClient.attach()
                .doOnSubscribe(foo -> {
                    nostrClient.send(event0).subscribe().dispose();
                })
                .bufferTimeout(1, Duration.ofSeconds(3))
                .defaultIfEmpty(Collections.emptyList())
                .blockFirst(Duration.ofSeconds(5)));

        OkResponse ok2 = response2.stream()
                .filter(it -> it.getKindCase() == Response.KindCase.OK)
                .map(Response::getOk)
                .findFirst()
                .orElseThrow();

        assertThat(ok2.getEventId(), is(event0.getId()));
        assertThat(ok2.getSuccess(), is(true));
        assertThat(ok2.getMessage(), is(""));
    }
}
