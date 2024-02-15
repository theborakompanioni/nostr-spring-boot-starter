package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.TextMessage;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public final class DefaultParseErrorHandler implements ParseErrorHandler {

    @Override
    public void handleParseError(NostrWebSocketSession session, TextMessage message, Exception e) throws Exception {
        session.sendResponseImmediately(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("Error while parsing message: %s".formatted(e.getMessage()))
                        .build())
                .build());
    }
}
