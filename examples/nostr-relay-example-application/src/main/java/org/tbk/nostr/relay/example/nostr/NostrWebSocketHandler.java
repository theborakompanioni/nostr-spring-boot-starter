package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.relay.example.nostr.handler.*;

public interface NostrWebSocketHandler extends WebSocketHandler,
        CloseRequestHandler, CountRequestHandler, EventRequestHandler, ReqRequestHandler, UnknownRequestHandler {

    void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception;

}
