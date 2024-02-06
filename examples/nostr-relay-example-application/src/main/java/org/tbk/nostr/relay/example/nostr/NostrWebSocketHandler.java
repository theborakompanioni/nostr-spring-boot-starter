package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;

public interface NostrWebSocketHandler extends WebSocketHandler {

    void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception;

    void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception;

    void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception;

    void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception;

    void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception;

    void handleUnknownMessage(WebSocketSession session, Request request) throws Exception;
}
