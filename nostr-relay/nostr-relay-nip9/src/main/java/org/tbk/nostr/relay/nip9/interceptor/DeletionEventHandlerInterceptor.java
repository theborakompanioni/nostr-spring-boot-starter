package org.tbk.nostr.relay.nip9.interceptor;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.nip9.Nip9Support;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DeletionEventHandlerInterceptor implements RequestHandlerInterceptor {

    @NonNull
    private final Nip9Support support;

    @Override
    public void postHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            handleEvent(request.getEvent().getEvent());
        }
    }

    private void handleEvent(Event event) {
        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);
        if (Nip9.isDeletionEvent(event)) {
            doOnDeletionEventCreated(publicKey, event);
        } else {
            onNonDeletionEventCreated(publicKey, event);
        }
    }

    private void doOnDeletionEventCreated(XonlyPublicKey publicKey, Event event) {
        List<TagValue> eTags = MoreTags.findByName(event, IndexedTag.e);
        List<TagValue> aTags = MoreTags.findByName(event, IndexedTag.a);

        if (eTags.isEmpty() && aTags.isEmpty()) {
            log.warn("Invalid state - missing `e` or `a` tag of deletion event: Did the validator not run?");
        } else {
            if (!eTags.isEmpty()) {
                Set<EventId> deletableEventIds = eTags.stream()
                        .map(it -> it.getValues(0))
                        .map(EventId::fromHex)
                        .collect(Collectors.toSet());

                if (deletableEventIds.isEmpty()) {
                    log.warn("Invalid state - invalid `e` tag of deletion event: Did the validator not run?");
                } else {
                    support.deleteAllByEventIds(publicKey, deletableEventIds).subscribe(unused -> {
                        log.debug("Marked events as deleted based on deletion event {}.", event.getId());
                    }, e -> {
                        log.warn("Error while marking events as deleted based on deletion event {}: {}", event.getId(), e.getMessage());
                    });
                }
            }

            if (!aTags.isEmpty()) {
                Set<EventUri> deletableEventUris = aTags.stream()
                        .map(it -> it.getValues(0))
                        .map(EventUri::parse)
                        .collect(Collectors.toSet());

                if (deletableEventUris.isEmpty()) {
                    log.warn("Invalid state - invalid `a` tag of deletion event: Did the validator not run?");
                } else {
                    support.deleteAllByEventUris(publicKey, deletableEventUris).subscribe(unused -> {
                        log.debug("Marked events as deleted based on deletion event {}.", event.getId());
                    }, e -> {
                        log.warn("Error while marking events as deleted based on deletion event {}: {}", event.getId(), e.getMessage());
                    });
                }
            }
        }
    }

    private void onNonDeletionEventCreated(XonlyPublicKey publicKey, Event event) {
        support.hasDeletionEvent(publicKey, event)
                .filter(it -> it)
                .doOnNext(it -> {
                    log.debug("Found existing deletion event for incoming event {}", event.getId());
                })
                .flatMap(it -> support.deleteAllByEventIds(publicKey, List.of(EventId.of(event))))
                .subscribe(unused -> {
                    log.debug("Successfully marked event {} as deleted.", event.getId());
                }, e -> {
                    log.warn("Error while marking event {} as deleted: {}", event.getId(), e.getMessage());
                });
    }
}
