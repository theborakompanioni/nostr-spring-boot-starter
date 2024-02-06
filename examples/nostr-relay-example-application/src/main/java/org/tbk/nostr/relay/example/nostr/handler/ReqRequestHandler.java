package org.tbk.nostr.relay.example.nostr.handler;

import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.ReqRequest;

public interface ReqRequestHandler {

    void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception;
}
