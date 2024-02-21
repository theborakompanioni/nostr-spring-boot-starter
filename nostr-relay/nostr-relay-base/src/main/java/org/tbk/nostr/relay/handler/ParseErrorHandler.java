package org.tbk.nostr.relay.handler;

import org.springframework.web.socket.TextMessage;
import org.tbk.nostr.relay.NostrRequestContext;

public interface ParseErrorHandler {

    void handleParseError(NostrRequestContext context, TextMessage message, Exception e) throws Exception;
}
