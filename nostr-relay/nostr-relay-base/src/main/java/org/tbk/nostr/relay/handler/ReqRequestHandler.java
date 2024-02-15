package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.relay.NostrWebSocketSession;

public interface ReqRequestHandler {

    void handleReqMessage(NostrWebSocketSession session, ReqRequest req) throws Exception;
}
