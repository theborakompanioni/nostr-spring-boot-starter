package org.tbk.nostr.relay.nip1.interceptor;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.nip1.Nip1Support;
import org.tbk.nostr.util.MorePublicKeys;

import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
public class EphemeralEventInterceptor implements RequestHandlerInterceptor {

    @NonNull
    private final Nip1Support support;

    @Override
    public void postHandle(NostrRequestContext context, Request request) {
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

        support.deleteAll(publicKey, event.getKind())
                .block(Duration.ofSeconds(60));
    }
}
