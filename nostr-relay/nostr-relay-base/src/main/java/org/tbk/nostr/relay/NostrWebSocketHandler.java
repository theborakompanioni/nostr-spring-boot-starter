package org.tbk.nostr.relay;

import org.tbk.nostr.relay.handler.*;

public interface NostrWebSocketHandler extends
        ReqRequestHandler,
        EventRequestHandler,
        CloseRequestHandler,
        CountRequestHandler,
        AuthRequestHandler,
        UnknownRequestHandler,
        ParseErrorHandler {
}
