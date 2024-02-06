package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.EventRequest;

public interface EventRequestHandler {

    void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception;
}
