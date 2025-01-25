package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip65;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip-test"})
class NostrRelayNip65Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    /**
     * NIP-64 states:
     * > The event MUST include a list of r tags with relay URIs [...]
     * Can be read as also being able to have an empty list of r tags.
     * Also, users might want to remove their relays altogether instead of
     * publishing a deletion event, which might not be respected.
     */
    @Test
    void itShouldAcceptEmptyRelayListEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizeRelayList(signer, Collections.emptyList());

        assertThat(event.getKind(), is(Kinds.kindRelayListMetadata.getValue()));

        OkResponse ok0 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event.getId()));
        assertThat(ok0.getSuccess(), is(true));
        assertThat(ok0.getMessage(), is(""));

        assertThat(Nip65.findRelays(event).getReadRelays(), hasSize(0));
        assertThat(Nip65.findRelays(event).getWriteRelays(), hasSize(0));
    }

    @Test
    void itShouldAcceptRelayListEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizeRelayList(signer, List.of(nostrTemplate.getRelayUri()));

        assertThat(event.getKind(), is(Kinds.kindRelayListMetadata.getValue()));

        OkResponse ok0 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event.getId()));
        assertThat(ok0.getSuccess(), is(true));
        assertThat(ok0.getMessage(), is(""));

        assertThat(Nip65.findRelays(event).getReadRelays(), containsInRelativeOrder(nostrTemplate.getRelayUri()));
        assertThat(Nip65.findRelays(event).getWriteRelays(), containsInRelativeOrder(nostrTemplate.getRelayUri()));
    }

    @Test
    void itShouldFetchRelayListEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.createFinalizeRelayList(signer,
                Nip65.ReadWriteRelays.builder()
                        .readRelays(List.of(nostrTemplate.getRelayUri(), RelayUri.parse("ws://dev.localhost:8080")))
                        .writeRelays(List.of(nostrTemplate.getRelayUri()))
                        .build());

        assertThat(event.getKind(), is(Kinds.kindRelayListMetadata.getValue()));

        OkResponse ok0 = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(ok0.getEventId(), is(event.getId()));
        assertThat(ok0.getSuccess(), is(true));

        Nip65.ReadWriteRelays readWriteRelays = nostrTemplate.fetchRelayListByAuthor(signer.getPublicKey())
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(readWriteRelays.getReadRelays(), containsInRelativeOrder(nostrTemplate.getRelayUri(), RelayUri.parse("ws://dev.localhost:8080")));
        assertThat(readWriteRelays.getWriteRelays(), containsInRelativeOrder(nostrTemplate.getRelayUri()));

        assertThat(Nip65.findRelays(event), is(readWriteRelays));
    }
}
