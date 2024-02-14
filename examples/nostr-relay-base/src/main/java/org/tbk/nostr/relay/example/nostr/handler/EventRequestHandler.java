package org.tbk.nostr.relay.example.nostr.handler;

import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface EventRequestHandler {

    void handleEventMessage(NostrWebSocketSession session, EventRequest event) throws Exception;
}
