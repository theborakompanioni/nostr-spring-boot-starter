package org.tbk.nostr.relay;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Response;

import java.util.Optional;

public interface NostrRequestContext {

    NostrWebSocketSession getSession();

    boolean add(Response message);

    void setHandledEvent(Event event);

    Optional<Event> getHandledEvent();

    boolean isAuthenticated();
}
