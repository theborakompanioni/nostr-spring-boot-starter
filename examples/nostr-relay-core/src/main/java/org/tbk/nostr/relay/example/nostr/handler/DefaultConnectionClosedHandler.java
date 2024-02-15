package org.tbk.nostr.relay.example.nostr.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class DefaultConnectionClosedHandler implements ConnectionClosedHandler {

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("WebSocket connection closed ({}): {}", closeStatus.getCode(), session.getId());
        }
    }
}
