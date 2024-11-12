package org.tbk.nostr.relay;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Response;

import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

public interface NostrRequestContext {

    NostrWebSocketSession getSession();

    boolean add(Response message);

    void setHandledEvent(Event event);

    Optional<Event> getHandledEvent();

    Optional<String> getAuthenticationChallenge();

    void setAuthenticationChallenge(String challenge);

    boolean isAuthenticated();

    void setAuthentication(Principal principal);

    default void clearAuthentication() {
        setAuthentication(null);
    }
}
