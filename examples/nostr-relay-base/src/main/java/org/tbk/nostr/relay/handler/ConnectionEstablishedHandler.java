package org.tbk.nostr.relay.handler;
import org.springframework.web.socket.WebSocketSession;

public interface ConnectionEstablishedHandler {
    /**
     * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is
     * opened and ready for use.
     *
     * @throws Exception this method can handle or propagate exceptions
     */
    void afterConnectionEstablished(WebSocketSession session) throws Exception;
}
