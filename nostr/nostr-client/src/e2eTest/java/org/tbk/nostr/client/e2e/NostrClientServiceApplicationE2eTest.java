package org.tbk.nostr.client.e2e;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.nips.Nip40;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrClientServiceApplicationE2eTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;


    @Autowired(required = false)
    private NostrClientService sut;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(sut, is(notNullValue()));
    }

    @Test
    void itShouldPublishNoteAndRetrieveSuccessfully() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip40.expire(
                Nip1.createTextNote(signer.getPublicKey(), "GM"),
                Instant.now().plusSeconds(60)
        ));

        sut.send(event).block(Duration.ofSeconds(5));

        SubscriptionId subscriptionId = MoreSubscriptionIds.random();

        ReqRequest reqRequest = ReqRequest.newBuilder()
                .setId(subscriptionId.getId())
                .addFilters(Filter.newBuilder()
                        .addKinds(Kinds.kindTextNote.getValue())
                        .addAuthors(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                        .build())
                .build();

        Event fetchedEvent = sut.subscribeToEvents(reqRequest)
                .blockFirst(Duration.ofSeconds(5));

        assertThat(fetchedEvent, is(notNullValue()));
        assertThat(fetchedEvent.getId(), is(event.getId()));
        assertThat(fetchedEvent.getContent(), is(event.getContent()));
    }
}
