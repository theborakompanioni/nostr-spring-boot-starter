package org.tbk.nostr.relay.example.nostr.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
public class DefaultConnectionEstablishedHandler implements ConnectionEstablishedHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("WebSocket connection established: {}", session.getId());
        }
    }
}
