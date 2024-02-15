package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface CloseRequestHandler {

    void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) throws Exception;
}
