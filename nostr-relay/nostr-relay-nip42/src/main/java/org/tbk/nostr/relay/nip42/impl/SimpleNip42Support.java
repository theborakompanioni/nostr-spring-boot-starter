package org.tbk.nostr.relay.nip42.impl;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.nip42.Nip42Support;
import reactor.core.publisher.Mono;

public class SimpleNip42Support implements Nip42Support {

    SimpleNip42ChallengeFactory challengeFactory = new SimpleNip42ChallengeFactory();

    @Override
    public String createNewChallenge(NostrWebSocketSession session) {
        return challengeFactory.create();
    }

    @Override
    public Mono<Boolean> handleAuthEvent(NostrWebSocketSession session, Event authEvent) {
        // TODO: implement!
        return Mono.error(new UnsupportedOperationException("Not yet implemented."));
    }

    @Override
    public Mono<Boolean> needsAuthentication(NostrRequestContext context, Request request) {
        // TODO: implement!
        return Mono.just(true);
    }
}
