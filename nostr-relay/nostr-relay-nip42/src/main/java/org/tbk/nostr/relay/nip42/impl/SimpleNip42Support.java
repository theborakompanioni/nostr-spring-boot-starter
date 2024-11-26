package org.tbk.nostr.relay.nip42.impl;

import org.tbk.nostr.nips.Nip70;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.nip42.Nip42Support;
import org.tbk.nostr.util.MorePublicKeys;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HexFormat;

public class SimpleNip42Support implements Nip42Support {

    private final SimpleNip42ChallengeFactory challengeFactory = new SimpleNip42ChallengeFactory();

    @Override
    public String createNewChallenge(NostrWebSocketSession session) {
        return HexFormat.of().formatHex(challengeFactory.create());
    }

    @Override
    public Mono<Boolean> requiresAuthentication(NostrRequestContext context, Request request) {
        if (request.getKindCase() != Request.KindCase.EVENT) {
            return Mono.just(false);
        }
        return Mono.just(Nip70.isProtectedEvent(request.getEvent().getEvent()));
    }

    @Override
    public Mono<NostrAuthentication> attemptAuthentication(NostrRequestContext context, Event authEvent) {
        return Mono.just(SimpleNostrAuthenticationToken.authenticated(
                MorePublicKeys.fromEvent(authEvent),
                authEvent,
                Collections.emptyList()
        ));
    }
}
