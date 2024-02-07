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

        assertThat(oks.size(), is(events.size()));
        oks.forEach(ok -> {
            assertThat(ok.getSuccess(), is(true));
        });

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

        assertThat(oks.size(), is(events.size()));
        oks.forEach(ok -> {
            assertThat(ok.getSuccess(), is(true));
        });

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

        assertThat(oks.size(), is(events.size()));
        oks.forEach(ok -> {
            assertThat(ok.getSuccess(), is(true));
        });

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

        assertThat(oks.size(), is(events.size()));
        oks.forEach(ok -> {
            assertThat(ok.getSuccess(), is(true));
        });

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

        assertThat(oks.size(), is(events.size()));
        oks.forEach(ok -> {
            assertThat(ok.getSuccess(), is(true));
        });

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
}
