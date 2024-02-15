package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface CountRequestHandler {

    void handleCountMessage(NostrWebSocketSession session, CountRequest count) throws Exception;
}
