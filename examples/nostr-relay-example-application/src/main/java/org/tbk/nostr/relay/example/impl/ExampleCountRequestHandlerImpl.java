package org.tbk.nostr.relay.example.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.relay.example.nostr.handler.CountRequestHandler;

@RequiredArgsConstructor
public class ExampleCountRequestHandlerImpl implements CountRequestHandler {

    @Override
    public void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
