package org.tbk.nostr.relay.example.nostr.support;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.handler.UnknownRequestHandler;

public final class DefaultUnknownRequestHandler implements UnknownRequestHandler {

    @Override
    public void handleUnknownMessage(WebSocketSession session, Request request) throws Exception {
        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("Error: %s".formatted("Cannot handle message of unknown type."))
                        .build())
                .build())));
    }
}
