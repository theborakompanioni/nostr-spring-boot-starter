package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.CountRequest;
import org.tbk.nostr.relay.NostrRequestContext;

public interface CountRequestHandler {

    void handleCountMessage(NostrRequestContext context, CountRequest count) throws Exception;
}
