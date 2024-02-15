package org.tbk.nostr.relay;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

public abstract class AbstractNostrWebSocketHandler implements NostrWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
