package org.tbk.nostr.relay.example.nostr.handler;

import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

public interface ReqRequestHandler {

    void handleReqMessage(NostrWebSocketSession session, ReqRequest req) throws Exception;
}
