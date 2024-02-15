package org.tbk.nostr.relay.example.impl;

import lombok.RequiredArgsConstructor;
import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;
import org.tbk.nostr.relay.handler.CloseRequestHandler;

@RequiredArgsConstructor
public class ExampleCloseRequestHandlerImpl implements CloseRequestHandler {

    @Override
    public void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
