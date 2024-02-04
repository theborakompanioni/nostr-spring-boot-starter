package org.tbk.nostr.template;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.util.MoreSubscriptionIds;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

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
    void itShouldPublishSimpleEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextMessage(signer, "GM");

        OkResponse ok = sut.send(event).block(Duration.ofSeconds(5));
        assertThat(ok, is(notNullValue()));
        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(true));

        Event fetchedEvent0 = sut.fetchEventById(EventId.of(ok.getEventId().toByteArray())).block(Duration.ofSeconds(5));
        assertThat(fetchedEvent0, is(event));

        Event fetchedEvent1 = sut.fetchEventByAuthor(signer.getPublicKey()).next().block(Duration.ofSeconds(5));
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
        assertThat(ok.getSuccess(), is(false));
    }

    @Test
    void itShouldFetchMultipleEventsByIdsSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextMessage(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextMessage(signer1, "GN");

        OkResponse ok0 = sut.send(event0).block(Duration.ofSeconds(5));
        assertThat(ok0, is(notNullValue()));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = sut.send(event1).block(Duration.ofSeconds(5));
        assertThat(ok1, is(notNullValue()));
        assertThat(ok1.getSuccess(), is(true));

        List<EventId> eventIds = Stream.of(ok0, ok1).map(OkResponse::getEventId)
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

        Event event0 = MoreEvents.createFinalizedTextMessage(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextMessage(signer1, "GN");

        OkResponse ok0 = sut.send(event0).block(Duration.ofSeconds(5));
        assertThat(ok0, is(notNullValue()));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = sut.send(event1).block(Duration.ofSeconds(5));
        assertThat(ok1, is(notNullValue()));
        assertThat(ok1.getSuccess(), is(true));

        List<EventId> eventIds = Stream.of(ok0, ok1).map(OkResponse::getEventId)
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

        Event event0 = MoreEvents.createFinalizedTextMessage(signer0, "GM");
        Event event1 = MoreEvents.createFinalizedTextMessage(signer1, "GN");

        OkResponse ok0 = sut.send(event0).block(Duration.ofSeconds(5));
        assertThat(ok0, is(notNullValue()));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = sut.send(event1).block(Duration.ofSeconds(5));
        assertThat(ok1, is(notNullValue()));
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
}
