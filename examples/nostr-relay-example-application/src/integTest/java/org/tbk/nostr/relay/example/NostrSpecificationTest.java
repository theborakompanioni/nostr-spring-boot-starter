package org.tbk.nostr.relay.example;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class NostrSpecificationTest {

    @LocalServerPort
    private int serverPort;

    private NostrTemplate nostrTemplate;

    @BeforeEach
    void beforeEach() {
        if (this.nostrTemplate == null) {
            this.nostrTemplate = new SimpleNostrTemplate(RelayUri.of("ws://localhost:%d".formatted(serverPort)));
        }
    }

    @Test
    void itShouldPublishSimpleEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(true));
        assertThat(ok.getMessage(), is(""));
    }

    @Test
    void itShouldNotifyOnDuplicateEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizedTextNote(signer, "GM");

        OkResponse ok0 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getEventId(), is(event.getId()));
        assertThat(ok0.getSuccess(), is(true));
        assertThat(ok0.getMessage(), is(""));

        OkResponse ok1 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok1.getEventId(), is(event.getId()));
        assertThat(ok1.getSuccess(), is(false));
        assertThat(ok1.getMessage(), is("Error: Duplicate event."));
    }

    @Test
    void itShouldNotifyOnInvalidEvent() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent = MoreEvents.createFinalizedTextNote(signer, "GM").toBuilder()
                .setContent("Changed!")
                .build();

        assertThat(MoreEvents.isValid(invalidEvent), is(false));

        OkResponse ok = nostrTemplate.send(invalidEvent)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(invalidEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("Error: Invalid signature."));
    }

    @Test
    void itShouldFetchEventByIdSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event eventMatching = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event eventNonMatching = MoreEvents.createFinalizedTextNote(signer, "GM1");

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Event fetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(eventMatching.getId().toByteArray()))
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(eventMatching));
    }

    @Test
    void itShouldFetchEventByAuthorSuccessfully0() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Event eventMatching = MoreEvents.createFinalizedTextNote(signer0, "GM");
        Event eventNonMatching = MoreEvents.createFinalizedTextNote(signer1, "GM");

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEventByAuthor(signer0.getPublicKey())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents.size(), is(1));

        Event fetchedEventSinceNow0 = fetchedEvents.getFirst();
        assertThat(fetchedEventSinceNow0, is(eventMatching));
    }

    @Test
    void itShouldFetchEventsByKindSuccessfully0() {
        Signer signer = SimpleSigner.random();

        int kind = 1337;

        Event eventMatching = MoreEvents.finalize(signer, MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .setKind(kind)
                .setContent("GM")));

        Event eventNonMatching = MoreEvents.finalize(signer, MoreEvents.withEventId(eventMatching.toBuilder()
                .setKind(eventMatching.getKind() + 1)));

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAllIds(events.stream()
                                        .map(Event::getId)
                                        .toList())
                                .addKinds(kind)
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents.size(), is(1));

        Event fetchedEvent0 = fetchedEvents.getFirst();
        assertThat(fetchedEvent0, is(eventMatching));
    }

    @Test
    void itShouldFetchEventsWithSinceSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();

        Event eventMatching = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.getEpochSecond()));

        Event eventNonMatching = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.minusSeconds(1).getEpochSecond()));

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAllIds(events.stream()
                                        .map(Event::getId)
                                        .toList())
                                .setSince(now.getEpochSecond())
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents.size(), is(1));

        Event fetchedEvent0 = fetchedEvents.getFirst();
        assertThat(fetchedEvent0, is(eventMatching));
    }

    @Test
    void itShouldFetchEventsWithUntilSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();

        Event eventMatching = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.getEpochSecond()));

        Event eventNonMatching = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.plusSeconds(1).getEpochSecond()));

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAllIds(events.stream()
                                        .map(Event::getId)
                                        .toList())
                                .setUntil(now.getEpochSecond())
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents.size(), is(1));

        Event fetchedEvent0 = fetchedEvents.getFirst();
        assertThat(fetchedEvent0, is(eventMatching));
    }

    @Test
    void itShouldFetchEventsWithLimitSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "GM2");

        List<Event> events = List.of(event0, event1, event2);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Filter eventsFilter = Filter.newBuilder()
                .addAllIds(events.stream()
                        .map(Event::getId)
                        .toList())
                .build();
        List<Event> fetchedEventsAll = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(eventsFilter)
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEventsAll.size(), is(events.size()));

        int limit = events.size() - 1;
        List<Event> fetchedEventsLimited = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(eventsFilter.toBuilder()
                                .setLimit(limit)
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEventsLimited.size(), is(limit));
    }

    @Test
    void itShouldFetchEventsSortedSuccessfully0WithSingleFilter() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.getEpochSecond()));
        Event event1Newer = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(event0.getCreatedAt() - 1));

        List<Event> events = List.of(event0, event1Newer, event2Older);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Filter eventsFilter = Filter.newBuilder()
                .addAllIds(events.stream()
                        .map(Event::getId)
                        .toList())
                .build();

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(eventsFilter)
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(events.size()));
        assertThat(fetchedEvents.get(0), is(event1Newer));
        assertThat(fetchedEvents.get(1), is(event0));
        assertThat(fetchedEvents.get(2), is(event2Older));
    }

    @Test
    void itShouldFetchEventsSortedSuccessfully0WithMultiFilter() {
        Signer signer = SimpleSigner.random();

        Instant now = Instant.now();

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(now.getEpochSecond()));
        Event event1Newer = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(event0.getCreatedAt() - 1));

        List<Event> events = List.of(event0, event1Newer, event2Older);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Filter.Builder eventsFilterBuilder = Filter.newBuilder()
                .addAllIds(events.stream()
                        .map(Event::getId)
                        .toList());

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(eventsFilterBuilder.setLimit(1))
                        .addFilters(eventsFilterBuilder.setLimit(2))
                        .addFilters(eventsFilterBuilder.setLimit(3))
                        .addFilters(eventsFilterBuilder)
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(events.size()));
        assertThat(fetchedEvents.get(0), is(event1Newer));
        assertThat(fetchedEvents.get(1), is(event0));
        assertThat(fetchedEvents.get(2), is(event2Older));
    }
}
