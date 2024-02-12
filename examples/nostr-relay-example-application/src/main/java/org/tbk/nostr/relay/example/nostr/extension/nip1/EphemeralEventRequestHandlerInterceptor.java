package org.tbk.nostr.relay.example.nostr.extension.nip1;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.util.MorePublicKeys;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class EphemeralEventRequestHandlerInterceptor implements NostrRequestHandlerInterceptor {

    @NonNull
    private final Nip1Support support;

    @Override
    public void postHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.EVENT) {
            handleEvent(request.getEvent().getEvent());
        }
    }

    private void handleEvent(Event event) {
        if (Nip1.isEphemeralEvent(event)) {
            doOnEphemeralEvent(event);
        }
    }

    private void doOnEphemeralEvent(Event event) {
        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);

        support.markDeletedBeforeCreatedAtInclusive(publicKey, event.getKind(), Instant.MAX)
                .block(Duration.ofSeconds(60));
    }
}
