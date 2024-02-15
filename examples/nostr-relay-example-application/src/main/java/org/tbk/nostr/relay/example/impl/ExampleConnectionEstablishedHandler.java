package org.tbk.nostr.relay.example.impl;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.relay.example.nostr.handler.DefaultConnectionEstablishedHandler;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public class ExampleConnectionEstablishedHandler extends DefaultConnectionEstablishedHandler {
    @Nullable
    private final String greeting;

    public ExampleConnectionEstablishedHandler(NostrRelayExampleApplicationProperties properties) {
        this.greeting = requireNonNull(properties).getGreeting().orElse(null);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        if (greeting != null) {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setNotice(NoticeResponse.newBuilder()
                            .setMessage(greeting)
                            .build())
                    .build())));
        }
    }
}
