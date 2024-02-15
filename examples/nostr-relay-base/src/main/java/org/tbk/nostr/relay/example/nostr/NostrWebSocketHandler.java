package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.relay.example.nostr.handler.*;

public interface NostrWebSocketHandler extends
        ReqRequestHandler,
        EventRequestHandler,
        CloseRequestHandler,
        CountRequestHandler,
        UnknownRequestHandler {

    void handleJsonParseException(NostrWebSocketSession session, TextMessage message, Exception e) throws Exception;

    /**
     * Invoked after WebSocket negotiation has succeeded and the WebSocket connection is
     * opened and ready for use.
     *
     * @throws Exception this method can handle or propagate exceptions
     */
    void afterConnectionEstablished(WebSocketSession session) throws Exception;

    /**
     * Invoked after the WebSocket connection has been closed by either side, or after a
     * transport error has occurred. Although the session may technically still be open,
     * depending on the underlying implementation, sending messages at this point is
     * discouraged and most likely will not succeed.
     *
     * @throws Exception this method can handle or propagate exceptions
     */
    void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception;
}
