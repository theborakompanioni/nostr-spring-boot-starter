package org.tbk.nostr.relay;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.Response;

import java.io.IOException;

public interface NostrWebSocketSession extends WebSocketSession {

    @Override
    default void setTextMessageSizeLimit(int messageSizeLimit) {
        throw new UnsupportedOperationException("'setTextMessageSizeLimit' has been disabled on purpose. Do it in method 'afterConnectionEstablished'.");
    }

    @Override
    default void setBinaryMessageSizeLimit(int messageSizeLimit) {
        throw new UnsupportedOperationException("'setBinaryMessageSizeLimit' has been disabled on purpose. Do it in method 'afterConnectionEstablished'.");
    }

    SessionId getSessionId();

    boolean queueResponse(Response message);

    void sendResponseImmediately(Response message) throws IOException;

    boolean isAuthenticated();

    void setAuthenticated(boolean authenticated);

    record SessionId(@NonNull String id) {
    }
}
