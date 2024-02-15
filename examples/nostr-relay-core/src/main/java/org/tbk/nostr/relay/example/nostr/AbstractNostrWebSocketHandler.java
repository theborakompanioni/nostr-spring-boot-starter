package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.NoticeResponse;
import org.tbk.nostr.proto.Response;

public abstract class AbstractNostrWebSocketHandler implements NostrWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
