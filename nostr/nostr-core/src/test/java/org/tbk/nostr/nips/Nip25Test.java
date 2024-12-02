package org.tbk.nostr.nips;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.*;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class Nip25Test {

    @Test
    void itShouldLikeAnEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        Event reactionEvent = MoreEvents.finalize(signer, Nip25.like(signer.getPublicKey(), event));

        assertThat(reactionEvent.getKind(), is(Kinds.kindReaction.getValue()));
        assertThat(reactionEvent.getContent(), is("+"));

        List<TagValue> eTags = MoreTags.findByName(reactionEvent, IndexedTag.e);
        assertThat(eTags, hasSize(1));
        assertThat(eTags.getLast().getValues(0), is(EventId.of(event.getId().toByteArray()).toHex()));

        List<TagValue> pTags = MoreTags.findByName(reactionEvent, IndexedTag.p);
        assertThat(pTags, hasSize(1));
        assertThat(pTags.getLast().getValues(0), is(signer.getPublicKey().value.toHex()));

        List<TagValue> kTags = MoreTags.findByName(reactionEvent, IndexedTag.k);
        assertThat(kTags, hasSize(1));
        assertThat(kTags.getLast().getValues(0), is(String.valueOf(event.getKind())));

        List<TagValue> aTags = MoreTags.findByName(reactionEvent, IndexedTag.a);
        assertThat(aTags, hasSize(0));
    }

    @Test
    void itShouldDislikeAnEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        Event reactionEvent = MoreEvents.finalize(signer, Nip25.dislike(signer.getPublicKey(), event));

        assertThat(reactionEvent.getKind(), is(Kinds.kindReaction.getValue()));
        assertThat(reactionEvent.getContent(), is("-"));

        List<TagValue> eTags = MoreTags.findByName(reactionEvent, IndexedTag.e);
        assertThat(eTags, hasSize(1));
        assertThat(eTags.getLast().getValues(0), is(EventId.of(event.getId().toByteArray()).toHex()));

        List<TagValue> pTags = MoreTags.findByName(reactionEvent, IndexedTag.p);
        assertThat(pTags, hasSize(1));
        assertThat(pTags.getLast().getValues(0), is(signer.getPublicKey().value.toHex()));

        List<TagValue> kTags = MoreTags.findByName(reactionEvent, IndexedTag.k);
        assertThat(kTags, hasSize(1));
        assertThat(kTags.getLast().getValues(0), is(String.valueOf(event.getKind())));

        List<TagValue> aTags = MoreTags.findByName(reactionEvent, IndexedTag.a);
        assertThat(aTags, hasSize(0));
    }

    @Test
    void itShouldReactToReplaceableEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedMetadata(signer, Metadata.newBuilder()
                .name("nostr-spring-boot-starter")
                .build());

        Event reactionEvent = MoreEvents.finalize(signer, Nip25.like(signer.getPublicKey(), event));

        assertThat(reactionEvent.getKind(), is(Kinds.kindReaction.getValue()));
        assertThat(reactionEvent.getContent(), is("+"));

        List<TagValue> eTags = MoreTags.findByName(reactionEvent, IndexedTag.e);
        assertThat(eTags, hasSize(1));
        assertThat(eTags.getLast().getValues(0), is(EventId.of(event.getId().toByteArray()).toHex()));

        List<TagValue> pTags = MoreTags.findByName(reactionEvent, IndexedTag.p);
        assertThat(pTags, hasSize(1));
        assertThat(pTags.getLast().getValues(0), is(signer.getPublicKey().value.toHex()));

        List<TagValue> aTags = MoreTags.findByName(reactionEvent, IndexedTag.a);
        assertThat(aTags, hasSize(1));
        assertThat(aTags.getLast().getValues(0), is("%d:%s".formatted(event.getKind(), signer.getPublicKey().value.toHex())));
    }

    @Test
    void itShouldIncludeTagsOfParentEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer,
                Nip1.createAddressableEvent(signer.getPublicKey(), "GM", "dTag")
                        .addTags(MoreTags.e(EventId.random().toHex()))
                        .addTags(MoreTags.e(EventId.random().toHex()))
                        .addTags(MoreTags.p(SimpleSigner.random().getPublicKey()))
                        .addTags(MoreTags.p(SimpleSigner.random().getPublicKey()))
                        .addTags(MoreTags.a(0, SimpleSigner.random().getPublicKey()))
                        .addTags(MoreTags.a(1, SimpleSigner.random().getPublicKey()))
                        .addTags(MoreTags.a(2, SimpleSigner.random().getPublicKey(), "dTag"))
                        .addTags(MoreTags.a(3, SimpleSigner.random().getPublicKey(), "dTag", RelayUri.parse("wss://example.org")))
        );

        Event reactionEvent = MoreEvents.finalize(signer, Nip25.like(signer.getPublicKey(), event));

        assertThat(reactionEvent.getKind(), is(Kinds.kindReaction.getValue()));
        assertThat(reactionEvent.getContent(), is("+"));

        List<TagValue> eTags = MoreTags.findByName(reactionEvent, IndexedTag.e);
        assertThat(eTags, hasSize(3));
        assertThat(eTags.getLast().getValues(0), is(EventId.of(event.getId().toByteArray()).toHex()));

        List<TagValue> pTags = MoreTags.findByName(reactionEvent, IndexedTag.p);
        assertThat(pTags, hasSize(3));
        assertThat(pTags.getLast().getValues(0), is(signer.getPublicKey().value.toHex()));

        List<TagValue> kTags = MoreTags.findByName(reactionEvent, IndexedTag.k);
        assertThat(kTags, hasSize(1));
        assertThat(kTags.getLast().getValues(0), is(String.valueOf(event.getKind())));

        List<TagValue> aTags = MoreTags.findByName(reactionEvent, IndexedTag.a);
        assertThat(aTags, hasSize(5));
        assertThat(aTags.getLast().getValues(0), is("%d:%s:dTag".formatted(event.getKind(), signer.getPublicKey().value.toHex())));
    }
}
