package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonReader;

public abstract class AbstractNostrWebSocketHandler extends TextWebSocketHandler {
    @Override
    protected final void handleTextMessage(WebSocketSession session, TextMessage message) {
        Request request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        switch (request.getKindCase()) {
            case EVENT -> handleEventMessage(session, request.getEvent());
            case REQ -> handleReqMessage(session, request.getReq());
            case CLOSE -> handleCloseMessage(session, request.getClose());
            case COUNT -> handleCountMessage(session, request.getCount());
            case KIND_NOT_SET -> handleUnknownMessage(session, request);
        }
    }

    protected abstract void handleEventMessage(WebSocketSession session, EventRequest event);

    protected abstract void handleReqMessage(WebSocketSession session, ReqRequest req);

    protected abstract void handleCloseMessage(WebSocketSession session, CloseRequest close);

    protected abstract void handleCountMessage(WebSocketSession session, CountRequest count);

    protected abstract void handleUnknownMessage(WebSocketSession session, Request request);
}
