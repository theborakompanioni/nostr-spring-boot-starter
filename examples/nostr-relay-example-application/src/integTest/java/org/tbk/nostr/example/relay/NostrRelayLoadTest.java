package org.tbk.nostr.example.relay;

import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@Slf4j
@EnabledIfEnvironmentVariable(named = "CI", matches = "true")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "load-test"})
class NostrRelayLoadTest {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldSendEventLoadTestSingleEvent() {
        Signer signer = SimpleSigner.random();

        Event event = createTestEvents(signer, 1).getFirst();

        Stopwatch started = Stopwatch.createStarted();
        OkResponse ok = nostrTemplate.send(event)
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();

        assertThat(ok.getEventId(), is(event.getId()));
        assertThat(ok.getSuccess(), is(true));
        assertThat(ok.getMessage(), is(""));

        log.info("Inserting 1 event took {}", started.stop());
    }

    @RepeatedTest(3)
    void itShouldSendEventLoadTestSingleConnection() throws InterruptedException {
        Signer signer = SimpleSigner.random();

        int eventCount = 1_000;

        List<Event> events = createTestEvents(signer, eventCount);

        CountDownLatch latch = new CountDownLatch(1);

        Stopwatch started = Stopwatch.createStarted();
        Flux.just(events)
                .subscribeOn(Schedulers.single())
                .flatMap(it -> nostrTemplate.send(it).timeout(Duration.ofSeconds(60)))
                .doFinally(foo -> {
                    latch.countDown();
                }).subscribe(ok -> {
                    assertThat(ok.getSuccess(), is(true));
                    assertThat(ok.getMessage(), is(""));
                });

        assertThat(latch.await(60, TimeUnit.SECONDS), is(true));

        log.info("Inserting {} events on single thread took {}", eventCount, started.stop());
    }

    @RepeatedTest(3)
    void itShouldSendEventLoadTestMultipleConnections() throws InterruptedException {
        Signer signer = SimpleSigner.random();

        int eventCount = 1_000;

        List<Event> events = createTestEvents(signer, eventCount);

        CountDownLatch latch = new CountDownLatch(1);

        Stopwatch started = Stopwatch.createStarted();
        Flux.fromIterable(events)
                .subscribeOn(Schedulers.newParallel("load-test"))
                .flatMap(it -> nostrTemplate.send(it).timeout(Duration.ofSeconds(60)))
                .doFinally(foo -> {
                    latch.countDown();
                }).subscribe(ok -> {
                    assertThat(ok.getSuccess(), is(true));
                    assertThat(ok.getMessage(), is(""));
                });

        assertThat(latch.await(60, TimeUnit.SECONDS), is(true));

        log.info("Inserting {} events on multiple threads took {}", eventCount, started.stop());
    }

    private static List<Event> createTestEvents(Signer signer, int eventCount) {
        Event prototype = Nip1.createTextNote(signer.getPublicKey(), "")
                .addTags(MoreTags.named("e", "00".repeat(32)))
                .addTags(MoreTags.named("e", "00".repeat(32)))
                .addTags(MoreTags.named("any", "1"))
                .addTags(MoreTags.named("any", "2"))
                .addTags(MoreTags.named("any", "3"))
                .addTags(MoreTags.named("z", "1"))
                .addTags(MoreTags.named("Z", "2"))
                .addTags(MoreTags.named("z", "1"))
                .addTags(MoreTags.named("Z", "2"))
                .addTags(MoreTags.named("s"))
                .buildPartial();

        return IntStream.range(0, eventCount)
                .mapToObj(i -> Event.newBuilder(prototype).setContent("GM" + i))
                .map(event -> MoreEvents.finalize(signer, event))
                .toList();
    }
}
