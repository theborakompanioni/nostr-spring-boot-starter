package org.tbk.nostr.relay.nip42;

import org.springframework.security.core.Authentication;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import reactor.core.publisher.Mono;


public interface Nip42Support {

    String createNewChallenge(NostrWebSocketSession session);

    Mono<Authentication> attemptAuthentication(NostrRequestContext context, Event authEvent);

    Mono<Boolean> requiresAuthentication(NostrRequestContext context, Request request);
}
