package org.tbk.nostr.example.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreSubscriptionIds;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
class MainApplicationRunner implements ApplicationRunner, DisposableBean {

    @NonNull
    private final NostrClientService nostrClientService;

    private Disposable subscription;

    @Override
    public void run(ApplicationArguments args) {
        SubscriptionId subscriptionId = MoreSubscriptionIds.random();

        ReqRequest reqRequest = ReqRequest.newBuilder()
                .setId(subscriptionId.getId())
                .addFilters(Filter.newBuilder()
                        .addKinds(1)
                        .setSince(Instant.now().minusSeconds(21).getEpochSecond())
                        .build())
                .build();

        log.info("[ALL] Will subscribe (subscription_id = '{}')…", subscriptionId.getId());
        this.subscription = nostrClientService.subscribe(reqRequest, NostrClientService.SubscribeOptions.defaultOptions().toBuilder()
                        .closeOnEndOfStream(false)
                        .build())
                .doOnSubscribe(foo -> {
                    log.info("[ALL] Subscribed.");
                })
                .delaySubscription(Duration.ofSeconds(5))
                .subscribe(it -> {
                    log.info("[ALL] New event: '{}'", it.getContent());
                }, e -> {
                    log.error("[ALL] Error during event stream: {}", e.getMessage());
                }, () -> {
                    log.info("[ALL] Completed.");
                });
    }

    @Override
    public void destroy() {
        if (subscription != null) {
            subscription.dispose();
        }
    }
}
