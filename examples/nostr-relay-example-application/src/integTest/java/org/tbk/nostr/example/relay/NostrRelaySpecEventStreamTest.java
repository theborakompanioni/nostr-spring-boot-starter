package org.tbk.nostr.example.relay;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.client.SimpleNostrClientService;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = {NostrRelaySpecEventStreamTest.NostrRelaySpecEventStreamTestConfig.class})
@ActiveProfiles({"test", "spec-test"})
public class NostrRelaySpecEventStreamTest {

    @Lazy // needed for @LocalServerPort to be populated
    @TestConfiguration(proxyBeanMethods = false)
    static class NostrRelaySpecEventStreamTestConfig {

        private final int serverPort;

        NostrRelaySpecEventStreamTestConfig(@LocalServerPort int serverPort) {
            this.serverPort = serverPort;
        }

        @Bean
        RelayUri relayUri() {
            return RelayUri.of("ws://localhost:%d".formatted(serverPort));
        }

        @Bean(destroyMethod = "stopAsync")
        SimpleNostrClientService nostrClientService(RelayUri relayUri) throws TimeoutException {
            SimpleNostrClientService client = new SimpleNostrClientService(relayUri);
            client.startAsync();
            client.awaitRunning(Duration.ofSeconds(60));
            return client;
        }
    }

    @Autowired
    private SimpleNostrClientService nostrClient;

    @Test
    void itShouldReceiveEventsSuccessfully0() throws InterruptedException {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.createFinalizedTextNote(signer, "GM0");
        Event event1 = MoreEvents.createFinalizedTextNote(signer, "GM1");
        Event event2 = MoreEvents.createFinalizedTextNote(signer, "GM2");

        CountDownLatch latch = new CountDownLatch(2);

        List<Event> receivedEvents = new ArrayList<>();
        nostrClient.subscribe(ReqRequest.newBuilder()
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
        nostrClient.subscribe(ReqRequest.newBuilder()
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

        assertThat(receivedEvents, hasSize(3));
        assertThat(receivedEvents, hasItem(event0));
        assertThat(receivedEvents, hasItem(event1));
        assertThat(receivedEvents, hasItem(event2));
    }
}
