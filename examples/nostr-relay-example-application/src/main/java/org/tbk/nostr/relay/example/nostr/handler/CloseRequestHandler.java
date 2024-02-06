package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.CloseRequest;

public interface CloseRequestHandler {

    void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception;
}
