package org.tbk.nostr.relay.nip42.impl;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        return Mono.just(true);
    }

    @Override
    public Mono<Authentication> attemptAuthentication(NostrRequestContext context, Event authEvent) {
        return Mono.just(UsernamePasswordAuthenticationToken.authenticated(
                MorePublicKeys.fromEvent(authEvent),
                "",
                Collections.emptyList()
        ));
    }
}
