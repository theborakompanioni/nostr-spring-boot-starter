package org.tbk.nostr.example.relay;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "spec-test"})
class NostrRelaySpecEventStreamTest {

    @Autowired
    private NostrClientService nostrClient;

    @Test
    void itShouldReceiveEventsSuccessfully0() throws InterruptedException {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "GM2");

        CountDownLatch latch = new CountDownLatch(2);

        List<Event> receivedEvents = new ArrayList<>();
        Disposable subscription = nostrClient.subscribe(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addIds(event1.getId())
                                .addIds(event2.getId())
                                .build())
                        .build(), NostrClientService.SubscribeOptions.defaultOptions().toBuilder()
                        .closeOnEndOfStream(false)
                        .build())
                .subscribe(it -> {
                    receivedEvents.add(it);
                    latch.countDown();
                });

        nostrClient.send(event0).block(Duration.ofSeconds(5));
        nostrClient.send(event1).block(Duration.ofSeconds(5));
        nostrClient.send(event2).block(Duration.ofSeconds(5));

        boolean awaited = latch.await(30, TimeUnit.SECONDS);
        if (!awaited) {
            throw new IllegalStateException("Could not await latch");
        }

        subscription.dispose();

        assertThat(receivedEvents, hasSize(2));
        assertThat(receivedEvents, hasItem(event1));
        assertThat(receivedEvents, hasItem(event2));
        assertThat(receivedEvents, not(hasItem(event0)));
    }

    @Test
    void itShouldVerifyDuplicateEventsWontGetEmittedToSubscribers() throws InterruptedException {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "GM2");

        CountDownLatch latch = new CountDownLatch(3);

        List<Event> receivedEvents = new ArrayList<>();
        Disposable subscription = nostrClient.subscribe(ReqRequest.newBuilder()
                        .setId(MoreSubscriptionIds.random().getId())
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.copyFrom(signer.getPublicKey().value.toByteArray()))
                                .build())
                        .build(), NostrClientService.SubscribeOptions.defaultOptions().toBuilder()
                        .closeOnEndOfStream(false)
                        .build())
                .subscribe(it -> {
                    receivedEvents.add(it);
                    latch.countDown();
                });

        nostrClient.send(event0).block(Duration.ofSeconds(5));
        nostrClient.send(event0).block(Duration.ofSeconds(5));

        nostrClient.send(event1).block(Duration.ofSeconds(5));

        nostrClient.send(event0).block(Duration.ofSeconds(5));
        nostrClient.send(event0).block(Duration.ofSeconds(5));

        nostrClient.send(event2).block(Duration.ofSeconds(5));

        nostrClient.send(event0).block(Duration.ofSeconds(5));
        nostrClient.send(event0).block(Duration.ofSeconds(5));

        boolean awaited = latch.await(30, TimeUnit.SECONDS);
        if (!awaited) {
            throw new IllegalStateException("Could not await latch");
        }

        subscription.dispose();

        assertThat("all events are received", receivedEvents, hasSize(3));
        assertThat("event0 has been received", receivedEvents, hasItem(event0));
        assertThat("event1 has been received", receivedEvents, hasItem(event1));
        assertThat("event2 has been received", receivedEvents, hasItem(event2));
    }
}
