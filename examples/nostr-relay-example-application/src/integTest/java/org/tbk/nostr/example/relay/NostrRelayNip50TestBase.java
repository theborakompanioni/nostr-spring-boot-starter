package org.tbk.nostr.example.relay;

import com.google.protobuf.ByteString;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NostrRelayNip50TestBase {

    static void itShouldSearchForEventSuccessfully0(NostrTemplate nostrTemplate) {
        Signer signer = SimpleSigner.random();

        Event eventMatching = MoreEvents.createFinalizedTextNote(signer, "0 This is a sentence in English that will match purple.");
        Event eventNonMatching = MoreEvents.createFinalizedTextNote(signer, "0 This is a sentence in English that will match orange.");

        List<Event> events = List.of(eventMatching, eventNonMatching);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        Event fetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(eventMatching.getId().toByteArray()))
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat("sanity check - event is stored successfully", fetchedEvent0, is(eventMatching));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(eventMatching.getPubkey())
                                .setSearch("purple")
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(is(1)));
        assertThat(fetchedEvents.getFirst(), is(eventMatching));
    }

    static void itShouldSearchForEventSuccessfully1(NostrTemplate nostrTemplate) {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "1 This is a sentence in English that will match purple.");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "1 This is a sentence in English that will match orange.");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "1 This is a sentence in English that will match black.");

        List<Event> events = List.of(event0, event1, event2);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(event0.getPubkey())
                                .setSearch("orange OR black")
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(is(2)));

        List<ByteString> fetchedEventsIds = fetchedEvents.stream().map(Event::getId).toList();
        assertThat(fetchedEventsIds, hasItem(event1.getId()));
        assertThat(fetchedEventsIds, hasItem(event2.getId()));
    }

    static void itShouldSearchForEventSuccessfully2Stemming(NostrTemplate nostrTemplate) {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "2 I think of orange turtles.");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "2 I thought of purple birds.");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "2 I was thinking of yellow mice.");

        List<Event> events = List.of(event0, event1, event2);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(oks.stream().filter(OkResponse::getSuccess).count(), is((long) events.size()));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(event0.getPubkey())
                                .setSearch("thinking")
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(is(2)));

        List<ByteString> fetchedEventsIds = fetchedEvents.stream().map(Event::getId).toList();
        assertThat(fetchedEventsIds, hasItem(event0.getId()));
        assertThat(fetchedEventsIds, hasItem(event2.getId()));
    }
}
