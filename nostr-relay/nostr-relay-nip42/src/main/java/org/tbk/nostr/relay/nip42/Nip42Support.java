package org.tbk.nostr.relay.nip42;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import reactor.core.publisher.Mono;


public interface Nip42Support {

    byte[] createNewChallenge(NostrWebSocketSession session);

    Mono<Boolean> handleAuthEvent(NostrRequestContext context, Event authEvent);

    Mono<Boolean> needsAuthentication(NostrRequestContext context, Request request);
}
