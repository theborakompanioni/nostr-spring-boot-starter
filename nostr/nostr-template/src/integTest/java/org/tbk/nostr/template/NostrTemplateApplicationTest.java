package org.tbk.nostr.template;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class NostrTemplateApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private NostrTemplate sut;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(sut, is(notNullValue()));
    }

    @Test
    @Disabled("nostr-rs-relay currently does not deliver a Relay Information Document")
    void itShouldFetchRelayInfoDocumentSuccessfully0() {
        RelayInfoDocument relayInfo = sut.fetchRelayInfoDocument()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(relayInfo, is(notNullValue()));
    }

    @Test
    void itShouldPublishSimpleEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));

        Event fetchedEvent0 = sut.fetchEventById(EventId.of(ok.getEventId().toByteArray())).block(Duration.ofSeconds(5));
        assertThat(fetchedEvent0, is(event));

        Event fetchedEvent1 = sut.fetchEventsByAuthor(signer.getPublicKey()).next().block(Duration.ofSeconds(5));
        assertThat(fetchedEvent1, is(event));
    }

    @Test
    void itShouldPublishSimpleEventSuccessfully1EscapeChars() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent("""
                        \n → \u000A
                        "" → \u0022
                        \\ → \u005C
                        \r → \u000D
                        \t → \u0009
                        \b → \u0008
                        \f → \u000C
                        """));

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));

        Event fetchedEvent = sut.fetchEventById(EventId.of(ok.getEventId().toByteArray())).block(Duration.ofSeconds(5));
        assertThat(fetchedEvent, is(event));
    }

    @Test
    void itShouldPublishEventWithTagsSuccessfully() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .addTags(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"))
                .addTags(MoreTags.named("alt", "reply"))
                .setKind(1)
                .setContent("GM"));

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));

        Event fetchedEvent = sut.fetchEventById(EventId.of(ok.getEventId().toByteArray())).block(Duration.ofSeconds(5));
        assertThat(fetchedEvent, is(event));
    }

    /**
     * Verify it is possible to send custom events, even if invalid.
     */
    @Test
    void itShouldFailToPublishEventWithInvalidEventId() {
        Signer signer = SimpleSigner.random();

        ByteString invalidEventId = ByteString.fromHex("beefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeefbeef");

        Event.Builder partialEvent = Event.newBuilder()
                .setId(invalidEventId)
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM");

        Event event = signer.sign(partialEvent).build();

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getEventId(), is(partialEvent.getId()));
        assertThat(ok.getMessage(), startsWith("invalid:"));
        assertThat(ok.getSuccess(), is(false));
    }

    @Test
    void itShouldSendMultipleEventsSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextNote(signer1, "GN");

        Set<Event> events = Set.of(event0, event1);
        List<OkResponse> oks = sut.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        for (OkResponse ok : oks) {
            assertThat(ok.getMessage(), is(""));
            assertThat(ok.getSuccess(), is(true));
            assertThat(ok.getEventId(), either(is(event0.getId())).or(is(event1.getId())));
        }
    }

    @Test
    void itShouldFetchMultipleEventsByIdsSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextNote(signer1, "GN");

        Set<Event> events = Set.of(event0, event1);
        List<OkResponse> oks = sut.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        for (OkResponse ok : oks) {
            assertThat(ok.getMessage(), is(""));
            assertThat(ok.getSuccess(), is(true));
        }

        List<EventId> eventIds = oks.stream()
                .map(OkResponse::getEventId)
                .map(it -> EventId.of(it.toByteArray()))
                .toList();

        List<Event> fetchedEvents = sut.fetchEventsByIds(eventIds)
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(fetchedEvents, is(notNullValue()));
        assertThat(fetchedEvents, hasSize(eventIds.size()));
        assertThat(fetchedEvents, hasItem(event0));
        assertThat(fetchedEvents, hasItem(event1));
    }

    @Test
    void itShouldFetchMultipleEventsByAuthorsSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextNote(signer1, "GN");

        Set<Event> events = Set.of(event0, event1);
        List<OkResponse> oks = sut.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        for (OkResponse ok : oks) {
            assertThat(ok.getMessage(), is(""));
            assertThat(ok.getSuccess(), is(true));
        }

        List<EventId> eventIds = oks.stream()
                .map(OkResponse::getEventId)
                .map(it -> EventId.of(it.toByteArray()))
                .toList();

        List<Event> fetchedEvents = sut.fetchEventsByAuthors(List.of(signer0.getPublicKey(), signer1.getPublicKey()))
                .collectList()
                .block(Duration.ofSeconds(5));

        assertThat(fetchedEvents, is(notNullValue()));
        assertThat(fetchedEvents, hasSize(eventIds.size()));
        assertThat(fetchedEvents, hasItem(event0));
        assertThat(fetchedEvents, hasItem(event1));
    }

    @Test
    @Disabled("scsibug/nostr-rs-relay does not support COUNT (NIP-45).. have not found a single relay that does..")
    void itShouldCountSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        CountResult countBefore = sut.countEvents(CountRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.fromHex(signer0.getPublicKey().value.toHex()))
                                .addAuthors(ByteString.fromHex(signer1.getPublicKey().value.toHex()))
                                .build())
                        .build())
                .next()
                .block(Duration.ofSeconds(5));

        assertThat(countBefore, is(notNullValue()));
        assertThat(countBefore.getCount(), is(0L));

        Event event0 = MoreEvents.createFinalizedTextNote(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextNote(signer1, "GN");

        OkResponse ok0 = sut.send(event0).block(Duration.ofSeconds(5));
        assertThat(ok0, is(notNullValue()));
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = sut.send(event1).block(Duration.ofSeconds(5));
        assertThat(ok1, is(notNullValue()));
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        CountResult countAfter = sut.countEvents(CountRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.fromHex(signer0.getPublicKey().value.toHex()))
                                .addAuthors(ByteString.fromHex(signer1.getPublicKey().value.toHex()))
                                .build())
                        .build())
                .next()
                .block(Duration.ofSeconds(5));

        assertThat(countAfter, is(notNullValue()));
        assertThat(countAfter.getCount(), is(2L));
    }

    @Test
    void itShouldFetchMetadataEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Optional<ProfileMetadata> metadataOrEmpty = sut.fetchMetadataByAuthor(signer.getPublicKey()).blockOptional(Duration.ofSeconds(5));
        assertThat(metadataOrEmpty, is(Optional.empty()));
    }

    @Test
    void itShouldFetchMetadataEventSuccessfully1() {
        Signer signer = SimpleSigner.random();

        ProfileMetadata metadata = ProfileMetadata.newBuilder()
                .setName("name")
                .setAbout("about")
                .setPicture(URI.create("https://www.example.com/picture.png").toString())
                .setDisplayName("display name")
                .setBanner(URI.create("https://www.example.com/banner.png").toString())
                .setWebsite(URI.create("https://www.example.com/").toString())
                .build();
        Event event = MoreEvents.createFinalizedMetadata(signer, metadata);

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getMessage(), is(""));
        assertThat(ok.getSuccess(), is(true));

        ProfileMetadata fetchedMetadata = sut.fetchMetadataByAuthor(signer.getPublicKey())
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedMetadata, is(metadata));
    }

    @Test
    void itShouldFetchMetadataEventSuccessfully2OnlyLatestMetadata() {
        Signer signer = SimpleSigner.random();

        ProfileMetadata metadata0 = ProfileMetadata.newBuilder()
                .setName("name")
                .setAbout("about")
                .setPicture(URI.create("https://www.example.com/example0.png").toString())
                .build();

        ProfileMetadata metadata1 = ProfileMetadata.newBuilder()
                .setName("name")
                .setAbout("about")
                .setPicture(URI.create("https://www.example.com/example1.png").toString())
                .build();

        assertThat("sanity check", metadata1, not(is(metadata0)));

        Event event0 = MoreEvents.createFinalizedMetadata(signer, metadata0);
        Event event1 = MoreEvents.finalize(signer, Nip1.createMetadata(signer.getPublicKey(), metadata1)
                // some relays reject events as duplicate if kind/created_at of same pubkey...
                // when in fact, it should replace it when it has a lower event id... so lame -_-
                .setCreatedAt(event0.getCreatedAt() + 1)
        );

        Set<Event> events = Set.of(event0, event1);
        List<OkResponse> oks = sut.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(true));
        }

        ProfileMetadata fetchedMetadata = sut.fetchMetadataByAuthor(signer.getPublicKey())
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedMetadata, is(metadata1));
    }
}
