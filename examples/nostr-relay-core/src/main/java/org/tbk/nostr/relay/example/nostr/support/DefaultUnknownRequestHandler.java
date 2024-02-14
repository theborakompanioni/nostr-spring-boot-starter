package org.tbk.nostr.relay.example.nostr.support;

import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;
import org.tbk.nostr.relay.example.nostr.handler.UnknownRequestHandler;

import java.io.IOException;

public final class DefaultUnknownRequestHandler implements UnknownRequestHandler {

    @Override
    public void handleUnknownMessage(NostrWebSocketSession session, Request request) throws IOException {
        session.sendResponseImmediately(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("Error: %s".formatted("Cannot handle message of unknown type."))
                        .build())
                .build());
    }
}
