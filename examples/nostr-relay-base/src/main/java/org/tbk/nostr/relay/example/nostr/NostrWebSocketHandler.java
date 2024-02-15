package org.tbk.nostr.relay.example.nostr;

import org.tbk.nostr.relay.example.nostr.handler.*;

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
