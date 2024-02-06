package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.proto.Request;

public interface UnknownRequestHandler {

    void handleUnknownMessage(WebSocketSession session, Request request) throws Exception;
}
