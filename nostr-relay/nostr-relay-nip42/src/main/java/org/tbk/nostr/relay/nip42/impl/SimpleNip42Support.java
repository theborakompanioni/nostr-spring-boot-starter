package org.tbk.nostr.relay.nip42.impl;

import org.tbk.nostr.nips.Nip42;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.nip42.Nip42Support;
import reactor.core.publisher.Mono;

import java.util.HexFormat;
import java.util.Optional;

public class SimpleNip42Support implements Nip42Support {

    SimpleNip42ChallengeFactory challengeFactory = new SimpleNip42ChallengeFactory();

    @Override
    public byte[] createNewChallenge(NostrWebSocketSession session) {
        return challengeFactory.create();
    }

    @Override
    public Mono<Boolean> handleAuthEvent(NostrRequestContext context, Event authEvent) {
        return Mono.defer(() -> {
            String expectedChallenge = context.getAuthChallenge()
                    .map(it -> HexFormat.of().formatHex(it))
                    .orElseThrow(() -> new IllegalStateException("No auth challenge associated."));

            String givenChallenge = Nip42.getChallenge(authEvent)
                    .orElseThrow(() -> new IllegalStateException("No auth challenge found."));

            // TODO: check everything according to https://github.com/nostr-protocol/nips/blob/master/42.md#signed-event-verification

            return Mono.just(expectedChallenge.equals(givenChallenge));
        });
    }

    @Override
    public Mono<Boolean> needsAuthentication(NostrRequestContext context, Request request) {
        return Mono.just(true);
    }
}
