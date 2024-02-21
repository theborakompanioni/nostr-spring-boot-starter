package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.relay.NostrRequestContext;

public interface EventRequestHandler {

    /**
     * A handler that processes incoming {@link org.tbk.nostr.proto.EventRequest} requests.
     * If an event has been handled successfully implementations must indicate this
     * by calling {@link NostrRequestContext#setHandledEvent(Event)}.
     *
     * @param context request context
     * @param event   incoming event
     * @throws Exception
     */
    void handleEventMessage(NostrRequestContext context, EventRequest event) throws Exception;
}
