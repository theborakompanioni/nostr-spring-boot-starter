package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface EventRequestHandler {

    void handleEventMessage(NostrWebSocketSession session, EventRequest event) throws Exception;
}
