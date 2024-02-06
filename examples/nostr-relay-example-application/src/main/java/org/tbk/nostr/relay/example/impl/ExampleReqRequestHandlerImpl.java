package org.tbk.nostr.relay.example.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.relay.example.nostr.handler.ReqRequestHandler;

@RequiredArgsConstructor
public class ExampleReqRequestHandlerImpl implements ReqRequestHandler {

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
