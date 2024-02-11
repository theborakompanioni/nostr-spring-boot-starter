package org.tbk.nostr.relay.example.extension.nip9;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.nips.Nip9;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class Nip9RequestHandlerInterceptor implements NostrRequestHandlerInterceptor {

    @NonNull
    private final Nip9Support support;

    @Override
    public void postHandle(WebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            handleEvent(request.getEvent().getEvent());
        }
    }

    private void handleEvent(Event event) {
        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);
        if (Nip9.isDeletionEvent(event)) {
            doOnDeletionEventCreated(event, publicKey);
        } else {
            onNonDeletionEventCreated(event, publicKey);
        }
    }

    private void doOnDeletionEventCreated(Event event, XonlyPublicKey publicKey) {
        List<TagValue> eTags = MoreTags.filterTagsByName(event, "e");
        // TODO: `a` tags ()

        Set<EventId> deletableEventIds = eTags.stream()
                .map(it -> it.getValues(0))
                .map(EventId::fromHex)
                .collect(Collectors.toSet());

        support.markDeleted(deletableEventIds, publicKey).subscribe(unused -> {
            log.debug("Marked events as deleted based on deletion event {}.", event.getId());
        }, e -> {
            log.warn("Error while marking events as deleted based on deletion event {}: {}", event.getId(), e.getMessage());
        });
    }


    private void onNonDeletionEventCreated(Event event, XonlyPublicKey publicKey) {
        support.hasDeletionEvent(event, publicKey)
                .filter(it -> it)
                .doOnNext(it -> {
                    log.debug("Found existing deletion event for incoming event {}", event.getId());
                })
                .flatMap(it -> support.markDeleted(List.of(EventId.of(event.getId().toByteArray())), publicKey))
                .subscribe(unused -> {
                    log.debug("Successfully marked event {} as deleted.", event.getId());
                }, e -> {
                    log.warn("Error while marking event {} as deleted: {}", event.getId(), e.getMessage());
                });
    }
}
