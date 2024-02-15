package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface UnknownRequestHandler {

    void handleUnknownMessage(NostrWebSocketSession session, Request request) throws Exception;
}
