package org.tbk.nostr.relay.example.nostr.handler;

import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface CountRequestHandler {

    void handleCountMessage(NostrWebSocketSession session, CountRequest count) throws Exception;
}
