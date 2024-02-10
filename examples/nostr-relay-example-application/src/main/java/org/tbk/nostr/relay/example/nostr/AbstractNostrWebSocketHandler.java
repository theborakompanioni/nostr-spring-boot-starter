package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;

public abstract class AbstractNostrWebSocketHandler extends TextWebSocketHandler implements NostrWebSocketHandler {
    @Override
    protected final void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Request request = null;
        try {
            request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        } catch (Exception e) {
            handleJsonParseException(session, message, e);
        }

        if (request != null) {
            switch (request.getKindCase()) {
                case EVENT -> handleEventMessage(session, request.getEvent());
                case REQ -> handleReqMessage(session, request.getReq());
                case CLOSE -> handleCloseMessage(session, request.getClose());
                case COUNT -> handleCountMessage(session, request.getCount());
                case KIND_NOT_SET -> handleUnknownMessage(session, request);
            }
        }
    }

    @Override
    public void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception {
        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("Error while parsing message: %s".formatted(e.getMessage()))
                        .build())
                .build())));
    }
}
