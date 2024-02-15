package org.tbk.nostr.relay.handler;

import org.springframework.web.socket.TextMessage;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface ParseErrorHandler {

    void handleParseError(NostrWebSocketSession session, TextMessage message, Exception e) throws Exception;
}
