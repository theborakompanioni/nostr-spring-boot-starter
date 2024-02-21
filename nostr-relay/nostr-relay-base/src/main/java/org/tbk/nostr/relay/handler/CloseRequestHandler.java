package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.CloseRequest;
import org.tbk.nostr.relay.NostrRequestContext;

public interface CloseRequestHandler {

    void handleCloseMessage(NostrRequestContext context, CloseRequest close) throws Exception;
}
