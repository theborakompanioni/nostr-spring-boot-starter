package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.TextMessage;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface ParseErrorHandler {

    void handleParseError(NostrWebSocketSession session, TextMessage message, Exception e) throws Exception;
}
