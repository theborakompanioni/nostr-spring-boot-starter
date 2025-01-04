package org.tbk.nostr.example.client;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreSubscriptionIds;
import reactor.core.Disposable;

import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

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
                        .addKinds(Kinds.kindTextNote.getValue())
                        .setSince(Instant.now().minusSeconds(21).getEpochSecond())
                        .build())
                .build();

        log.info("[ALL] Will subscribe (subscription_id = '{}')…", subscriptionId.getId());
        this.subscription = nostrClientService.subscribeToEvents(reqRequest, NostrClientService.SubscribeOptions.defaultOptions().toBuilder()
                        .closeOnEndOfStream(false)
                        .build())
                .doOnSubscribe(foo -> {
                    log.info("[ALL] Subscribed.");
                })
                .delaySubscription(Duration.ofSeconds(5))
                .subscribe(it -> {
                    EventId eventId = EventId.of(it);
                    String noteId = Nip19.encodeNote(eventId);
                    String displayId = "%s (%s)".formatted(noteId, eventId.toHex());

                    String pubkeyHex = HexFormat.of().formatHex(it.getPubkey().toByteArray());
                    String npub = Nip19.encodeNpub(MorePublicKeys.fromHex(pubkeyHex));
                    String displayPubkey = "%s (%s)".formatted(npub, pubkeyHex);
                    log.info("""
                            [ALL] New event:
                            id: {}
                            pubkey: {}
                            content: \"\"\"
                            {}
                            \"\"\"
                            """, displayId, displayPubkey, it.getContent());
                }, e -> {
                    log.error("[ALL] Error during event stream.", e);
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
