package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip18;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip-test"})
class NostrRelayNip18Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldAcceptRepostEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = MoreEvents.createFinalizedRepost(signer, event, nostrTemplate.getRelayUri());

        assertThat(repost.getKind(), is(Nip18.kindRepost().getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(true));
    }

    @Test
    void itShouldAcceptGenericRepostEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedMetadata(signer, Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build());
        Event genericRepost = MoreEvents.createFinalizedRepost(signer, event, nostrTemplate.getRelayUri());

        assertThat(genericRepost.getKind(), is(Nip18.kindGenericRepost().getValue()));

        OkResponse ok0 = nostrTemplate.send(genericRepost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(genericRepost.getId()));
        assertThat(ok0.getSuccess(), is(true));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidNote0InvalidId() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent = MoreEvents.createFinalizedTextNote(signer, "GM0").toBuilder()
                .setContent("GM1")
                .build();

        assertThat("sanity check", MoreEvents.hasValidSignature(invalidEvent), is(false));

        Event repost = MoreEvents.createFinalizedRepost(signer, invalidEvent, nostrTemplate.getRelayUri());

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Invalid id."));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidNote1InvalidSig() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent = MoreEvents.createFinalizedTextNote(signer, "GM0").toBuilder()
                .setSig(MoreEvents.createFinalizedTextNote(signer, "GM1").getSig())
                .build();

        assertThat("sanity check", MoreEvents.hasValidSignature(invalidEvent), is(false));

        Event repost = MoreEvents.createFinalizedRepost(signer, invalidEvent, nostrTemplate.getRelayUri());

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Invalid signature."));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidNote2InvalidETag() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .addTags(MoreTags.e("invalid")));
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Invalid tag 'e'."));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidKind() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip1.createEphemeralEvent(signer.getPublicKey(), "GM"));
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .setKind(Nip18.kindRepost().getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Reposted event must be a short text note."));
    }

    @Test
    void itShouldDeclineGenericRepostEventWithInvalidKind() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .setKind(Nip18.kindGenericRepost().getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Reposted event must not be a short text note."));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidETag() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .addTags(MoreTags.e("invalid")));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("invalid: Invalid tag 'e'."));
    }
}
