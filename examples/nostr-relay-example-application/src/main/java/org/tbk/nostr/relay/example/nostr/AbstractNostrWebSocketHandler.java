package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;

public abstract class AbstractNostrWebSocketHandler implements NostrWebSocketHandler {

    @Override
    public void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception {
        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("Error while parsing message: %s".formatted(e.getMessage()))
                        .build())
                .build())));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    }

}
