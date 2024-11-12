package org.tbk.nostr.relay;

import lombok.NonNull;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.Response;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

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

    void setAuthentication(Principal authenticated);

    default void clearAuthentication() {
        setAuthentication(null);
    }

    void setAuthenticationChallenge(String challenge);

    Optional<String> getAuthenticationChallenge();

    record SessionId(@NonNull String id) {
    }
}
