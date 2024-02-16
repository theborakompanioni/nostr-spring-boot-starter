package org.tbk.nostr.relay.handler;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface ConnectionClosedHandler {
    /**
     * Invoked after the WebSocket connection has been closed by either side, or after a
     * transport error has occurred. Although the session may technically still be open,
     * depending on the underlying implementation, sending messages at this point is
     * discouraged and most likely will not succeed.
     *
     * @throws Exception this method can handle or propagate exceptions
     */
    void afterConnectionClosed(NostrWebSocketSession session, CloseStatus closeStatus) throws Exception;
}
