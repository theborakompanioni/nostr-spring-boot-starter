package org.tbk.nostr.relay.handler;

import org.tbk.nostr.proto.AuthRequest;
import org.tbk.nostr.relay.NostrRequestContext;

public interface AuthRequestHandler {

    /**
     * A handler that processes incoming {@link org.tbk.nostr.proto.AuthRequest} requests.
     *
     * @param context request context
     * @param event   incoming event
     * @throws Exception
     */
    void handleAuthMessage(NostrRequestContext context, AuthRequest event) throws Exception;
}
