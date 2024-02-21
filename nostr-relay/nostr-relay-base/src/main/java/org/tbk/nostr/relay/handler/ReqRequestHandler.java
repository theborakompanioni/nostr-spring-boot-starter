package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.relay.NostrRequestContext;

public interface ReqRequestHandler {

    void handleReqMessage(NostrRequestContext context, ReqRequest req) throws Exception;
}
