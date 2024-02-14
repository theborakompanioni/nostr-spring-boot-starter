package org.tbk.nostr.relay.example.nostr.handler;

import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface CloseRequestHandler {

    void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) throws Exception;
}
