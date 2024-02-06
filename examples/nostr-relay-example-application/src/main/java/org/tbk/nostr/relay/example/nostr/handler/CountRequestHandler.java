package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.CountRequest;

public interface CountRequestHandler {

    void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception;
}
