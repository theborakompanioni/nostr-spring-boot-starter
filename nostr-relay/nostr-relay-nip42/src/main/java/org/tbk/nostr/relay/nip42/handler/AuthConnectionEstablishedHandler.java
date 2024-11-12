package org.tbk.nostr.relay.nip42.handler;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.proto.AuthResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.handler.ConnectionEstablishedHandler;
import org.tbk.nostr.relay.nip42.Nip42Support;

@RequiredArgsConstructor
public class AuthConnectionEstablishedHandler implements ConnectionEstablishedHandler {

    @NonNull
    private final Nip42Support nip42Support;

    @Override
    public void afterConnectionEstablished(NostrWebSocketSession session) throws Exception {
        String challenge = nip42Support.createNewChallenge(session);
        session.setAuthenticationChallenge(challenge);

        session.sendResponseImmediately(Response.newBuilder()
                .setAuth(AuthResponse.newBuilder()
                        .setChallenge(challenge)
                        .build())
                .build());
    }
}
