package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.example.nostr.handler.*;

@RequiredArgsConstructor
public class NostrRequestHandlerSupport implements NostrWebSocketHandler {

    @NonNull
    private final ConnectionEstablishedHandler connectionEstablishedHandler;

    @NonNull
    private final ConnectionClosedHandler connectionClosedHandler;

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
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connectionEstablishedHandler.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        connectionClosedHandler.afterConnectionClosed(session, closeStatus);
    }

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
