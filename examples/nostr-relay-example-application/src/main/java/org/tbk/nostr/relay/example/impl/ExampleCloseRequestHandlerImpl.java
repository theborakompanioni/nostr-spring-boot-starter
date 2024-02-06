package org.tbk.nostr.relay.example.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.relay.example.nostr.handler.CloseRequestHandler;

@RequiredArgsConstructor
public class ExampleCloseRequestHandlerImpl implements CloseRequestHandler {

    @Override
    public void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
