package org.tbk.nostr.relay.example.impl;

import lombok.RequiredArgsConstructor;
import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.handler.CountRequestHandler;

@RequiredArgsConstructor
public class ExampleCountRequestHandlerImpl implements CountRequestHandler {

    @Override
    public void handleCountMessage(NostrWebSocketSession session, CountRequest count) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
