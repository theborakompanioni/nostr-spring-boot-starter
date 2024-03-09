package org.tbk.nostr.client;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import reactor.core.Disposable;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrClientServiceIntegrationTest {

    @Autowired
    private RelayUri relayUri;

    @Test
    void itShouldStartAndStopServiceWithoutErrors() throws TimeoutException {
        SimpleNostrClientService sut = new SimpleNostrClientService(relayUri);
        sut.startAsync();
        sut.awaitRunning(Duration.ofSeconds(5));

        sut.stopAsync();
        sut.awaitTerminated(Duration.ofSeconds(5));
    }

    @Test
    void itShouldVerifyWebSocketSession() throws TimeoutException {
        SimpleNostrClientService sut = new SimpleNostrClientService(relayUri);
        sut.startAsync();
        sut.awaitRunning(Duration.ofSeconds(5));

        assertThat(sut.getSession(), is(notNullValue()));
        assertThat(sut.getSession().isOpen(), is(true));

        sut.stopAsync();
        sut.awaitTerminated(Duration.ofSeconds(5));

        assertThat(sut.getSession().isOpen(), is(false));
    }

    @Test
    void itShouldVerifySubscriptions() throws TimeoutException, InterruptedException {
        SimpleNostrClientService sut = new SimpleNostrClientService(relayUri);
        sut.startAsync();
        sut.awaitRunning(Duration.ofSeconds(5));

        assertThat(sut.getSubscriptions().isEmpty(), is(true));

        CountDownLatch latch = new CountDownLatch(1);
        Disposable subscriptionDisposable = sut.subscribe(ReqRequest.newBuilder()
                        .setId("test")
                        .addFilters(Filter.newBuilder()
                                .addKinds(1)
                                .build())
                        .build())
                .doOnSubscribe(it -> latch.countDown())
                .subscribe();

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("Timeout while waiting for latch");
        }

        assertThat(subscriptionDisposable.isDisposed(), is(false));
        assertThat(sut.getSubscriptions().size(), is(1));

        sut.stopAsync();
        sut.awaitTerminated(Duration.ofSeconds(5));

        assertThat(subscriptionDisposable.isDisposed(), is(true));
        assertThat(sut.getSubscriptions().isEmpty(), is(true));
    }

    @Test
    void itShouldVerifySubscriptionsIsEmptyIfNoReqRequestHasBeenSent() throws TimeoutException, InterruptedException {
        SimpleNostrClientService sut = new SimpleNostrClientService(relayUri);
        sut.startAsync();
        sut.awaitRunning(Duration.ofSeconds(5));

        assertThat(sut.getSubscriptions().isEmpty(), is(true));

        String subscriptionId = "test";

        CountDownLatch latch = new CountDownLatch(1);
        Disposable subscriptionDisposable = sut.attachTo(SubscriptionId.of(subscriptionId))
                .doOnSubscribe(it -> latch.countDown())
                .subscribe();

        if (!latch.await(5, TimeUnit.SECONDS)) {
            throw new TimeoutException("Timeout while waiting for latch");
        }

        assertThat(subscriptionDisposable.isDisposed(), is(false));
        assertThat(sut.getSubscriptions().isEmpty(), is(true));

        sut.stopAsync();
        sut.awaitTerminated(Duration.ofSeconds(5));

        assertThat(subscriptionDisposable.isDisposed(), is(true));
        assertThat(sut.getSubscriptions().isEmpty(), is(true));
    }
}
