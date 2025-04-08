package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nip18.Nip18;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ProfileMetadata;
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
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri()));

        assertThat(repost.getKind(), is(Kinds.kindRepost.getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getSuccess(), is(true));
    }

    @Test
    void itShouldAcceptGenericRepostEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedMetadata(signer, ProfileMetadata.newBuilder()
                .setName("name")
                .setAbout("about")
                .setPicture(URI.create("https://www.example.com/example.png").toString())
                .build());
        Event genericRepost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri()));

        assertThat(genericRepost.getKind(), is(Kinds.kindGenericRepost.getValue()));

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

        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), invalidEvent, nostrTemplate.getRelayUri()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Invalid id."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidNote1InvalidSig() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent = MoreEvents.createFinalizedTextNote(signer, "GM0").toBuilder()
                .setSig(MoreEvents.createFinalizedTextNote(signer, "GM1").getSig())
                .build();

        assertThat("sanity check", MoreEvents.hasValidSignature(invalidEvent), is(false));

        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), invalidEvent, nostrTemplate.getRelayUri()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Invalid signature."));
        assertThat(ok0.getSuccess(), is(false));
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
        assertThat(ok0.getMessage(), is("invalid: Invalid tag 'e'."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineRepostEventWithInvalidKind() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip1.createEphemeralEvent(signer.getPublicKey(), "GM"));
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .setKind(Kinds.kindRepost.getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Reposted event must be a short text note."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineGenericRepostEventWithInvalidKind() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .setKind(Kinds.kindGenericRepost.getValue()));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Reposted event must not be a short text note."));
        assertThat(ok0.getSuccess(), is(false));
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
        assertThat(ok0.getMessage(), is("invalid: Invalid tag 'e'."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineRepostEventWithMissingETag() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event, nostrTemplate.getRelayUri())
                .clearTags());

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Invalid 'e' tag. Must include reposted event id."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineRepostEventWithETagNotReferencingRepostedNote() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event0, nostrTemplate.getRelayUri())
                .clearTags()
                .addTags(MoreTags.e(event1)));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Invalid 'e' tag. Must include reposted event id."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldDeclineRepostEventWithETagMissingRelayUri() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event repost = MoreEvents.finalize(signer, Nip18.repost(signer.getPublicKey(), event0, nostrTemplate.getRelayUri())
                .clearTags()
                .addTags(MoreTags.named(IndexedTag.e.name(), EventId.of(event0).toHex())));

        OkResponse ok0 = nostrTemplate.send(repost)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(repost.getId()));
        assertThat(ok0.getMessage(), is("invalid: Invalid 'e' tag. Missing relay URL."));
        assertThat(ok0.getSuccess(), is(false));
    }
}
