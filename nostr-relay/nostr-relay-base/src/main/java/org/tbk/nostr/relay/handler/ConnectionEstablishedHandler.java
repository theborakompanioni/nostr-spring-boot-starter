package org.tbk.nostr.relay.handler;

import org.tbk.nostr.relay.NostrWebSocketSession;

public interface ConnectionEstablishedHandler {
    /**
     * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is
     * opened and ready for use.
     *
     * @throws Exception this method can handle or propagate exceptions
     */
    void afterConnectionEstablished(NostrWebSocketSession session) throws Exception;
}
