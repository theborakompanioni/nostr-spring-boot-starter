package org.tbk.nostr.relay;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.handler.*;

@RequiredArgsConstructor
public class NostrRequestHandlerSupport implements NostrWebSocketHandler {

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

    @NonNull
    private final ParseErrorHandler parseErrorHandler;

    @Override
    public final void handleParseError(NostrWebSocketSession session, TextMessage message, Exception e) throws Exception {
        parseErrorHandler.handleParseError(session, message, e);
    }

    @Override
    public final void handleReqMessage(NostrWebSocketSession session, ReqRequest req) throws Exception {
        reqRequestHandler.handleReqMessage(session, req);
    }

    @Override
    public final void handleEventMessage(NostrWebSocketSession session, EventRequest event) throws Exception {
        eventRequestHandler.handleEventMessage(session, event);
    }

    @Override
    public final void handleCloseMessage(NostrWebSocketSession session, CloseRequest close) throws Exception {
        closeRequestHandler.handleCloseMessage(session, close);
    }

    @Override
    public final void handleCountMessage(NostrWebSocketSession session, CountRequest count) throws Exception {
        countRequestHandler.handleCountMessage(session, count);
    }

    @Override
    public final void handleUnknownMessage(NostrWebSocketSession session, Request request) throws Exception {
        unknownRequestHandler.handleUnknownMessage(session, request);
    }
}
