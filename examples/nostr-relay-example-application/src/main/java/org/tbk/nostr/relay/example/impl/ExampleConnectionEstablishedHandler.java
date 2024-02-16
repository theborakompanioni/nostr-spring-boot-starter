package org.tbk.nostr.relay.example.impl;

import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.relay.handler.ConnectionEstablishedHandler;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class ExampleConnectionEstablishedHandler implements ConnectionEstablishedHandler {
    @Nullable
    private final String greeting;

    public ExampleConnectionEstablishedHandler(NostrRelayExampleApplicationProperties properties) {
        this.greeting = requireNonNull(properties).getGreeting().orElse(null);
    }

    @Override
    public void afterConnectionEstablished(NostrWebSocketSession session) throws Exception {
        if (greeting != null) {
            session.sendResponseImmediately(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(greeting)
                            .build())
                    .build());
        }
    }
}
