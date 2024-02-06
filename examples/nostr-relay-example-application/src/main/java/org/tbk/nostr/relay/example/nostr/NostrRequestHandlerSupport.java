package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.example.nostr.handler.*;

@RequiredArgsConstructor
public abstract class NostrRequestHandlerSupport extends AbstractNostrWebSocketHandler {

    @NonNull
    private final ReqRequestHandler reqRequestHandler;

    @NonNull
    private final EventRequestHandler eventRequestHandler;

    @NonNull
    private final CloseRequestHandler closeRequestHandler;

    @NonNull
    private final CountRequestHandler countRequestHandler;

    @NonNull
    private final UnknownRequestHandler unknownRequestHandler;

    @Override
    public final void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        reqRequestHandler.handleReqMessage(session, req);
    }

    @Override
    public final void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        eventRequestHandler.handleEventMessage(session, event);
    }

    @Override
    public final void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception {
        closeRequestHandler.handleCloseMessage(session, close);
    }

    @Override
    public final void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception {
        countRequestHandler.handleCountMessage(session, count);
    }

    @Override
    public final void handleUnknownMessage(WebSocketSession session, Request request) throws Exception {
        unknownRequestHandler.handleUnknownMessage(session, request);
    }
}
