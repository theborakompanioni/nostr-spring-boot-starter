package org.tbk.nostr.relay.example;

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
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "nip-test"})
public class NostrRelayNip9Test {

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
    void itShouldValidateExistingEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event invalidDeletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEvent(signer.getPublicKey(), List.of()));
        OkResponse ok1 = nostrTemplate.send(invalidDeletionEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getEventId(), is(invalidDeletionEvent0.getId()));
        assertThat(ok1.getMessage(), is("Error: Missing 'e' or 'a' tag."));
        assertThat(ok1.getSuccess(), is(false));

        EventId invalidDeletionEvent0Id = EventId.of(invalidDeletionEvent0.getId().toByteArray());
        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(invalidDeletionEvent0Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent0.isPresent(), is(false));
    }

    @Test
    void itShouldDeleteExistingEventSuccessfully0() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");

        List<Event> events = List.of(event0, event1);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        assertThat("events are ok", oks.stream().allMatch(OkResponse::getSuccess));

        EventId event0Id = EventId.of(event0.getId().toByteArray());
        Event fetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(event0));

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEvent(signer.getPublicKey(), List.of(event0Id)));
        OkResponse ok1 = nostrTemplate.send(deletionEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getEventId(), is(deletionEvent0.getId()));
        assertThat(ok1.getSuccess(), is(true));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(event0Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent0.isPresent(), is(false));

        EventId event1Id = EventId.of(event1.getId().toByteArray());
        Optional<Event> refetchedEvent1 = nostrTemplate.fetchEventById(event1Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent1.isPresent(), is(true));

        EventId deletionEvent0Id = EventId.of(deletionEvent0.getId().toByteArray());
        Event refetchedDeletionEvent0 = nostrTemplate.fetchEventById(deletionEvent0Id)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(refetchedDeletionEvent0, is(deletionEvent0));
    }

    @Test
    void itShouldDeleteExistingEventSuccessfully1IfDeletionEventAlreadyExists() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM");

        EventId event0Id = EventId.of(event0.getId().toByteArray());
        Optional<Event> fetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5));
        assertThat(fetchedEvent0.isPresent(), is(false));

        // publish the deletion event before the referenced event is published
        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEvent(signer.getPublicKey(), List.of(event0Id)));

        OkResponse ok1 = nostrTemplate.send(deletionEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok1.getEventId(), is(deletionEvent0.getId()));
        assertThat(ok1.getSuccess(), is(true));

        OkResponse ok0 = nostrTemplate.send(event0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok0.getEventId(), is(event0.getId()));
        assertThat(ok0.getSuccess(), is(true));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5));

        assertThat(refetchedEvent0.isPresent(), is(false));
    }

    @Test
    void itShouldDeleteExistingEventSuccessfully2ReplaceableEvent() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createReplaceableEvent(signer.getPublicKey(), "GM"));
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");

        List<Event> events = List.of(event0, event1);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        assertThat("events are ok", oks.stream().allMatch(OkResponse::getSuccess));

        EventId event0Id = EventId.of(event0.getId().toByteArray());
        Event fetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(event0));

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEventForEvent(event0));

        OkResponse ok1 = nostrTemplate.send(deletionEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getEventId(), is(deletionEvent0.getId()));
        assertThat(ok1.getSuccess(), is(true));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(event0Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent0.isPresent(), is(false));

        EventId event1Id = EventId.of(event1.getId().toByteArray());
        Optional<Event> refetchedEvent1 = nostrTemplate.fetchEventById(event1Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent1.isPresent(), is(true));
    }

    @Test
    void itShouldDeleteExistingEventSuccessfully2ParameterizedReplaceableEvent() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip1.createParameterizedReplaceableEvent(signer.getPublicKey(), "GM", "test"));
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");

        List<Event> events = List.of(event0, event1);
        List<OkResponse> oks = nostrTemplate.send(events)
                .collectList()
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(oks, hasSize(events.size()));
        assertThat("events are ok", oks.stream().allMatch(OkResponse::getSuccess));

        EventId event0Id = EventId.of(event0.getId().toByteArray());
        Event fetchedEvent0 = nostrTemplate.fetchEventById(event0Id)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(fetchedEvent0, is(event0));

        Event deletionEvent0 = MoreEvents.finalize(signer, Nip9.createDeletionEventForEvent(event0));

        OkResponse ok1 = nostrTemplate.send(deletionEvent0)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok1.getEventId(), is(deletionEvent0.getId()));
        assertThat(ok1.getSuccess(), is(true));

        Optional<Event> refetchedEvent0 = nostrTemplate.fetchEventById(event0Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent0.isPresent(), is(false));

        EventId event1Id = EventId.of(event1.getId().toByteArray());
        Optional<Event> refetchedEvent1 = nostrTemplate.fetchEventById(event1Id).blockOptional(Duration.ofSeconds(5));
        assertThat(refetchedEvent1.isPresent(), is(true));
    }
}
