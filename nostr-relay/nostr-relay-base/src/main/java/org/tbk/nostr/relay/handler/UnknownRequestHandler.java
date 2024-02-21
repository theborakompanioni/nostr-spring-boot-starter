package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;

public interface UnknownRequestHandler {

    void handleUnknownMessage(NostrRequestContext context, Request request) throws Exception;
}
