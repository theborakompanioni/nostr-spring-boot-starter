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
    public final void handleParseError(NostrRequestContext context, TextMessage message, Exception e) throws Exception {
        parseErrorHandler.handleParseError(context, message, e);
    }

    @Override
    public final void handleReqMessage(NostrRequestContext context, ReqRequest req) throws Exception {
        reqRequestHandler.handleReqMessage(context, req);
    }

    @Override
    public final void handleEventMessage(NostrRequestContext context, EventRequest event) throws Exception {
        eventRequestHandler.handleEventMessage(context, event);
    }

    @Override
    public final void handleCloseMessage(NostrRequestContext context, CloseRequest close) throws Exception {
        closeRequestHandler.handleCloseMessage(context, close);
    }

    @Override
    public final void handleCountMessage(NostrRequestContext context, CountRequest count) throws Exception {
        countRequestHandler.handleCountMessage(context, count);
    }

    @Override
    public final void handleUnknownMessage(NostrRequestContext context, Request request) throws Exception {
        unknownRequestHandler.handleUnknownMessage(context, request);
    }
}
