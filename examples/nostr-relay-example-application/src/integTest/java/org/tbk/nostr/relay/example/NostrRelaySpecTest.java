package org.tbk.nostr.relay.example;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip13;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.config.NostrRelayProperties;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreKinds;
import org.tbk.nostr.util.MoreSubscriptionIds;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {NostrRelaySpecTest.NostrRelaySpecTestConfig.class})
@ActiveProfiles({"test", "spec-test"})
public class NostrRelaySpecTest {

    @Lazy // needed for @LocalServerPort to be populated
    @TestConfiguration(proxyBeanMethods = false)
    static class NostrRelaySpecTestConfig {

        private final int serverPort;

        NostrRelaySpecTestConfig(@LocalServerPort int serverPort) {
            this.serverPort = serverPort;
        }

        @Bean
        RelayUri relayUri() {
            return RelayUri.of("ws://localhost:%d".formatted(serverPort));
        }

        @Bean
        NostrTemplate nostrTemplate(RelayUri relayUri) {
            return new SimpleNostrTemplate(relayUri);
        }
    }

    @Autowired
    private NostrRelayProperties relayProperties;

    @Autowired
    private NostrTemplate nostrTemplate;

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
    void itShouldNotifyOnInvalidEvent0Id() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent = MoreEvents.createFinalizedTextNote(signer, "GM").toBuilder()
                .setContent("Changed!")
                .build();

        assertThat("sanity check", MoreEvents.hasValidSignature(invalidEvent), is(false));

        OkResponse ok = nostrTemplate.send(invalidEvent)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(invalidEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("Error: Invalid id."));
    }

    @Test
    void itShouldNotifyOnInvalidEvent1Kind() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0").setKind(-1));
        Event invalidEvent1 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM1").setKind(65_536));

        List<Event> events = List.of(invalidEvent0, invalidEvent1);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));

        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid kind."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent2CreatedAt() {
        Signer signer = SimpleSigner.random();

        Event verifiedEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")).toBuilder()
                .setCreatedAt(-1L)
                .build();

        List<Event> events = List.of(verifiedEvent0);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));

        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid created timestamp."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent3Sig() {
        Signer signer = SimpleSigner.random();

        Event verifiedEvent0 = MoreEvents.verifySignature(MoreEvents.createFinalizedTextNote(signer, "GM"));
        assertThat("sanity check", MoreEvents.hasValidSignature(verifiedEvent0), is(true));

        Event verifiedEvent1 = MoreEvents.verifySignature(MoreEvents.createFinalizedTextNote(signer, verifiedEvent0.getContent() + "!"));
        assertThat("sanity check", MoreEvents.hasValidSignature(verifiedEvent1), is(true));

        assertThat("sanity check - id differs", verifiedEvent1.getId(), not(is(verifiedEvent0.getId())));
        assertThat("sanity check - sig differs", verifiedEvent1.getSig(), not(is(verifiedEvent0.getSig())));

        Event invalidEvent = verifiedEvent1.toBuilder()
                .setSig(verifiedEvent0.getSig())
                .build();

        assertThat("sanity check", MoreEvents.hasValidSignature(invalidEvent), is(false));

        OkResponse ok = nostrTemplate.send(invalidEvent)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(invalidEvent.getId()));
        assertThat(ok.getSuccess(), is(false));
        assertThat(ok.getMessage(), is("Error: Invalid signature."));
    }

    @Test
    void itShouldNotifyOnInvalidEvent3InvalidETag() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .addTags(MoreTags.e("")));
        Event invalidEvent1 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM1")
                .addTags(MoreTags.e("zz".repeat(32))));
        Event invalidEvent2 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM2")
                .addTags(MoreTags.e("00".repeat(16))));
        Event invalidEvent3 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM3")
                .addTags(MoreTags.e("zz".repeat(32)))
                .addTags(MoreTags.e("00".repeat(16))));

        List<Event> events = List.of(invalidEvent0, invalidEvent1, invalidEvent2, invalidEvent3);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));

        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid tag 'e'."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent4InvalidPTag() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .addTags(MoreTags.p("")));
        Event invalidEvent1 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM1")
                .addTags(MoreTags.p("zz".repeat(32))));
        Event invalidEvent2 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM2")
                .addTags(MoreTags.p("00".repeat(16))));
        Event invalidEvent3 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM3")
                .addTags(MoreTags.p("zz".repeat(32)))
                .addTags(MoreTags.p("00".repeat(16))));
        Event invalidEvent4 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM4")
                .addTags(MoreTags.p("00".repeat(32))));

        List<Event> events = List.of(invalidEvent0, invalidEvent1, invalidEvent2, invalidEvent3, invalidEvent4);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));

        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid tag 'p'."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent5InvalidATag() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .addTags(MoreTags.a("")));
        Event invalidEvent1 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM1")
                .addTags(MoreTags.a(MoreKinds.minValue() - 1, signer.getPublicKey())));
        Event invalidEvent2 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM2")
                .addTags(MoreTags.a(MoreKinds.maxValue() + 1, signer.getPublicKey())));
        Event invalidEvent3 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM3")
                .addTags(MoreTags.a("", signer.getPublicKey().value.toHex())));
        Event invalidEvent4 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM4")
                .addTags(MoreTags.a("NaN", signer.getPublicKey().value.toHex())));
        Event invalidEvent5 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM5")
                .addTags(MoreTags.a(":")));

        List<Event> events0 = List.of(invalidEvent0, invalidEvent1, invalidEvent2, invalidEvent3, invalidEvent4, invalidEvent5);
        List<OkResponse> oks0 = nostrTemplate.send(events0)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks0, hasSize(events0.size()));

        for (OkResponse ok : oks0) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid tag 'a'."));
        }

        Event invalidEvent6 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM6")
                .addTags(MoreTags.a("%d:%s".formatted(1, "00".repeat(32)))));

        List<Event> events1 = List.of(invalidEvent6);
        List<OkResponse> oks1 = nostrTemplate.send(events1)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks1, hasSize(events1.size()));

        for (OkResponse ok : oks1) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid pubkey in tag 'a'."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent6InvalidTagName() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .addTags(MoreTags.named("")));
        Event invalidEvent1 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM1")
                .addTags(MoreTags.named("", "test")));
        Event invalidEvent2 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM2")
                .addTags(MoreTags.named("0".repeat(257))));

        List<Event> events = List.of(invalidEvent0, invalidEvent1, invalidEvent2);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));

        for (OkResponse ok : oks) {
            assertThat(ok.getSuccess(), is(false));
            assertThat(ok.getMessage(), is("Error: Invalid tag name."));
        }
    }

    @Test
    void itShouldNotifyOnInvalidEvent7CreatedAtLessThanLowerLimit() {
        assertThat("sanity check", relayProperties.getCreatedAtLowerLimit(), is(notNullValue()));

        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .setCreatedAt(1));

        OkResponse ok0 = nostrTemplate.send(invalidEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        String expectedMessage = "Error: 'created_at' timestamp %d is less than lower limit %d.".formatted(
                invalidEvent0.getCreatedAt(),
                relayProperties.getCreatedAtLowerLimit()
        );
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is(expectedMessage));
    }

    @Test
    void itShouldNotifyOnInvalidEvent8CreatedAtGreaterThanUpperLimit() {
        assertThat("sanity check", relayProperties.getCreatedAtUpperLimit(), is(notNullValue()));

        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(Long.MAX_VALUE));

        OkResponse ok0 = nostrTemplate.send(invalidEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        String expectedMessage = "Error: 'created_at' timestamp %d is greater than upper limit %d.".formatted(
                invalidEvent0.getCreatedAt(),
                relayProperties.getCreatedAtUpperLimit()
        );
        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is(expectedMessage));
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
    void itShouldFetchEventByIdSuccessfully1Verify() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM0")
                .addTags(MoreTags.named("e", "00".repeat(32)))
                .addTags(MoreTags.named("e", "00".repeat(32)))
                .addTags(MoreTags.named("a", "%d:%s".formatted(MoreKinds.maxValue(), signer.getPublicKey().value.toHex())))
                .addTags(MoreTags.named("any", "1"))
                .addTags(MoreTags.named("any", "2"))
                .addTags(MoreTags.named("any", "3"))
                .addTags(MoreTags.named("z", "1"))
                .addTags(MoreTags.named("Z", "2"))
                .addTags(MoreTags.named("z", "1"))
                .addTags(MoreTags.named("Z", "2"))
                .addTags(MoreTags.named("s")));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getSuccess(), is(true));

        Event fetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(event0.getId().toByteArray()))
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(event0));
        assertThat(MoreEvents.hasValidSignature(fetchedEvent0), is(true));
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

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(eventMatching));
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

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(eventMatching));
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

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(eventMatching));
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

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(eventMatching));
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

        assertThat(fetchedEventsAll, hasSize(events.size()));

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

        assertThat(fetchedEventsLimited, hasSize(limit));
    }

    @Test
    void itShouldFetchEventsSortedSuccessfully0WithSingleFilter() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM"));
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() - 1));

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

        Event event0 = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM"));
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() - 1));

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

    @RepeatedTest(5)
    void itShouldVerifyEphemeralEventBehaviour0() {
        Signer signer = SimpleSigner.random();

        Event ephemeralEvent0 = MoreEvents.finalize(signer, MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .setKind(20_000)
                .setContent("GM")));

        OkResponse ok0 = nostrTemplate.send(ephemeralEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getEventId(), is(ephemeralEvent0.getId()));
        assertThat(ok0.getSuccess(), is(true));
        assertThat(ok0.getMessage(), is(""));

        Optional<Event> fetchedEvent0 = nostrTemplate.fetchEventById(EventId.of(ephemeralEvent0.getId().toByteArray()))
                .blockOptional(Duration.ofSeconds(5));

        assertThat(fetchedEvent0.isPresent(), is(false));
    }

    @RepeatedTest(5)
    void itShouldVerifyReplaceableEventBehaviour0() {
        Signer signer = SimpleSigner.random();

        Metadata metadata0 = Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build();

        Event event0 = MoreEvents.createFinalizedMetadata(signer, metadata0);
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));

        assertThat("sanity check", event1Newer.getCreatedAt(), is(greaterThan(event0.getCreatedAt())));

        List<Event> events = List.of(event0, event1Newer);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        // ok0 might contain an error flag - depending on which event arrives first! But ok1 MUST be successful.
        OkResponse ok1 = oks.stream()
                .filter(it -> event1Newer.getId().equals(it.getEventId()))
                .findFirst().orElseThrow();
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        assertThat(oks.stream().anyMatch(OkResponse::getSuccess), is(true));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event1Newer));
    }

    @RepeatedTest(5)
    void itShouldVerifyReplaceableEventBehaviour1NewerEventsExist() {
        Signer signer = SimpleSigner.random();

        Metadata metadata = Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build();

        Event event0 = MoreEvents.createFinalizedMetadata(signer, metadata);
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() - 1));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = nostrTemplate.send(event1Newer)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        OkResponse ok2 = nostrTemplate.send(event2Older)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok2.getMessage(), is("Error: A newer version of this replaceable event already exists."));
        assertThat(ok2.getSuccess(), is(false));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event1Newer));
    }

    @RepeatedTest(5)
    void itShouldVerifyReplaceableEventBehaviour2LowerIdWithSameCreatedAtTimestampCanBeInserted() {
        Signer signer = SimpleSigner.random();

        Metadata metadata = Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build();

        Event event1WithLowerId = MoreEvents.finalize(signer, Nip13.mineEvent(Nip1.createMetadata(signer.getPublicKey(), metadata), 16));
        Event event0 = MoreEvents.finalize(signer, Nip1.createMetadata(signer.getPublicKey(), metadata)
                .setCreatedAt(event1WithLowerId.getCreatedAt()));

        assertThat("sanity check", event1WithLowerId.getCreatedAt(), is(event0.getCreatedAt()));
        // this check might fail - unlikely, but can happen!
        assertThat("sanity check", EventId.of(event1WithLowerId.getId().toByteArray()), is(lessThan(EventId.of(event0.getId().toByteArray()))));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = nostrTemplate.send(event1WithLowerId)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event1WithLowerId));
    }

    @RepeatedTest(5)
    void itShouldVerifyReplaceableEventBehaviour3GreaterIdWithSameCreatedAtTimestampCanNotBeInserted() {
        Signer signer = SimpleSigner.random();

        Metadata metadata = Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build();

        Event event0WithLowerId = MoreEvents.finalize(signer, Nip13.mineEvent(Nip1.createMetadata(signer.getPublicKey(), metadata), 16));
        Event event1 = MoreEvents.finalize(signer, Nip1.createMetadata(signer.getPublicKey(), metadata)
                .setCreatedAt(event0WithLowerId.getCreatedAt()));

        assertThat("sanity check", event0WithLowerId.getCreatedAt(), is(event1.getCreatedAt()));
        // this check might fail - unlikely, but can happen!
        assertThat("sanity check", EventId.of(event0WithLowerId.getId().toByteArray()), is(lessThan(EventId.of(event1.getId().toByteArray()))));

        OkResponse ok0 = nostrTemplate.send(event0WithLowerId)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok2 = nostrTemplate.send(event1)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok2.getMessage(), is("Error: A version of this replaceable event with same timestamp and lower id already exists."));
        assertThat(ok2.getSuccess(), is(false));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event1.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event0WithLowerId));
    }

    @RepeatedTest(5)
    void itShouldVerifyParameterizedReplaceableEventBehaviour0() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "d"));
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));

        assertThat("sanity check", event1Newer.getCreatedAt(), is(greaterThan(event0.getCreatedAt())));

        List<Event> events = List.of(event0, event1Newer);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        // ok0 might contain an error flag - depending on which event arrives first! But ok1 MUST be successful.
        OkResponse ok1 = oks.stream()
                .filter(it -> event1Newer.getId().equals(it.getEventId()))
                .findFirst().orElseThrow();
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        assertThat(oks.stream().anyMatch(OkResponse::getSuccess), is(true));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event1Newer));
    }

    @RepeatedTest(5)
    void itShouldVerifyParameterizedReplaceableEventBehaviour1NewerEventsDoNotExist() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "test"));
        Event event1Older = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() - 1));
        Event event2DifferentTag = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM",
                        MoreTags.findByNameSingle(event0, IndexedTag.d.name())
                                .map(it -> it.getValues(0))
                                .orElseThrow()
                                .repeat(2)
                )
                .setCreatedAt(event0.getCreatedAt()));

        assertThat("sanity check", event1Older.getTagsList(), is(event0.getTagsList()));
        assertThat("sanity check", event1Older.getCreatedAt(), is(lessThan(event0.getCreatedAt())));
        assertThat("sanity check", event2DifferentTag.getTagsList(), is(not(event0.getTagsList())));
        assertThat("sanity check", event2DifferentTag.getCreatedAt(), is(event0.getCreatedAt()));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok2 = nostrTemplate.send(event1Older)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok2.getMessage(), is("Error: A newer version of this replaceable event already exists."));
        assertThat(ok2.getSuccess(), is(false));

        OkResponse ok3 = nostrTemplate.send(event2DifferentTag)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok3.getMessage(), is(""));
        assertThat(ok3.getSuccess(), is(true));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(2));
        assertThat(fetchedEvents, hasItem(event0));
        assertThat(fetchedEvents, hasItem(event2DifferentTag));
        assertThat(fetchedEvents, not(hasItem(event1Older)));
    }

    @RepeatedTest(5)
    void itShouldVerifyParameterizedReplaceableEventBehaviour2NewerEventsExist() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "d"));
        Event event1Newer = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() + 1));
        Event event2Older = MoreEvents.finalize(signer, event0.toBuilder().setCreatedAt(event0.getCreatedAt() - 1));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));

        OkResponse ok1 = nostrTemplate.send(event1Newer)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getMessage(), is(""));
        assertThat(ok1.getSuccess(), is(true));

        OkResponse ok2 = nostrTemplate.send(event2Older)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok2.getMessage(), is("Error: A newer version of this replaceable event already exists."));
        assertThat(ok2.getSuccess(), is(false));

        List<Event> fetchedEvents = nostrTemplate.fetchEvents(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(event0.getKind())
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(fetchedEvents, hasSize(1));
        assertThat(fetchedEvents.getFirst(), is(event1Newer));
    }

    @Test
    void itShouldVerifyParameterizedReplaceableEventBehaviour3MissingDTag() {
        Signer signer = SimpleSigner.random();

        Event invalidEvent0 = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "<will me removed>")
                .clearTags());

        OkResponse ok0 = nostrTemplate.send(invalidEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getMessage(), is("Error: Missing 'd' tag."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldVerifyParameterizedReplaceableEventBehaviour3MissingFirstValueOfDTag() {
        Signer signer = SimpleSigner.random();

        Event event0WithEmptyDTag = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", null));

        OkResponse ok0 = nostrTemplate.send(event0WithEmptyDTag)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getMessage(), is(""));
        assertThat(ok0.getSuccess(), is(true));
    }

    @Test
    @Disabled("NostrTemplate should have a method to send plain json")
    void itShouldVerifyParameterizedReplaceableEventBehaviour4NullFirstValueOfDTag() {
        Signer signer = SimpleSigner.random();

        Event prototype = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "test"));
        /*
        // TODO: must serialize as json and send plain
        OkResponse ok0 = nostrTemplate.send("TODO.. plain json here.. send a `d` tag like `["d", null, "value"]`")
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getSuccess(), is(false));
        assertThat(ok0.getMessage(), is("TODO.. what error message?"));
         */
    }

    @Test
    void itShouldVerifyParameterizedReplaceableEventBehaviour5MultipleDTags() {
        Signer signer = SimpleSigner.random();

        Event.Builder prototype = Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", null).clearTags();
        Event event0WithEmptyDTag = MoreEvents.finalize(signer, prototype
                .addTags(MoreTags.d("0"))
                .addTags(MoreTags.d()));

        OkResponse ok0 = nostrTemplate.send(event0WithEmptyDTag)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getMessage(), is("Error: Multiple 'd' tags are not allowed."));
        assertThat(ok0.getSuccess(), is(false));
    }

    @Test
    void itShouldVerifyCountIsNotSupportedByDefault() {
        Signer signer0 = SimpleSigner.random();
        Signer signer1 = SimpleSigner.random();

        Optional<CountResult> countBefore = nostrTemplate.countEvents(CountRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.fromHex(signer0.getPublicKey().value.toHex()))
                                .addAuthors(ByteString.fromHex(signer1.getPublicKey().value.toHex()))
                                .build())
                        .build())
                .next()
                .blockOptional(Duration.ofSeconds(5));

        assertThat("COUNT is not supported", countBefore.isEmpty());
    }
}
