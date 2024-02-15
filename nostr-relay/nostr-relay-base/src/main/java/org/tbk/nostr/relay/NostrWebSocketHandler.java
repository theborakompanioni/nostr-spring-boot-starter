package org.tbk.nostr.relay;

import org.tbk.nostr.relay.handler.*;

public interface NostrWebSocketHandler extends
        ConnectionEstablishedHandler,
        ConnectionClosedHandler,
        ReqRequestHandler,
        EventRequestHandler,
        CloseRequestHandler,
        CountRequestHandler,
        UnknownRequestHandler,
        ParseErrorHandler {
}
