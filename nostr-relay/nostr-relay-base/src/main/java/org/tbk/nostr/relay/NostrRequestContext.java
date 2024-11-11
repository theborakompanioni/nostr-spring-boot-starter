package org.tbk.nostr.relay;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Response;

import java.util.Optional;
import java.util.function.Function;

public interface NostrRequestContext {

    NostrWebSocketSession getSession();

    boolean add(Response message);

    void setHandledEvent(Event event);

    Optional<Event> getHandledEvent();

    boolean isAuthenticated();

    // TODO: move this one level up
    Optional<byte[]> getAuthChallenge();

    byte[] getOrComputeAuthChallenge(Function<NostrRequestContext, byte[]> factory);
}
