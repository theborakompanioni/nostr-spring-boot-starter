package org.tbk.nostr.relay.example.nostr.handler;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface UnknownRequestHandler {

    void handleUnknownMessage(NostrWebSocketSession session, Request request) throws Exception;
}
