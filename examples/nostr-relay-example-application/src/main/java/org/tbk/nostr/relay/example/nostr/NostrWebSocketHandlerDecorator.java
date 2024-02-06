package org.tbk.nostr.relay.example.nostr;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.tbk.nostr.proto.*;

/**
 * Wraps another {@link NostrWebSocketHandler} instance and delegates to it.
 */
public class NostrWebSocketHandlerDecorator extends WebSocketHandlerDecorator implements NostrWebSocketHandler {

    public NostrWebSocketHandlerDecorator(NostrWebSocketHandler delegate) {
        super(delegate);
    }

    @Override
    public NostrWebSocketHandler getDelegate() {
        return (NostrWebSocketHandler) super.getDelegate();
    }

    @Override
    public void handleJsonParseException(WebSocketSession session, TextMessage message, Exception e) throws Exception {
        getDelegate().handleJsonParseException(session, message, e);
    }

    @Override
    public void handleEventMessage(WebSocketSession session, EventRequest event) throws Exception {
        getDelegate().handleEventMessage(session, event);
    }

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        getDelegate().handleReqMessage(session, req);
    }

    @Override
    public void handleCloseMessage(WebSocketSession session, CloseRequest close) throws Exception {
        getDelegate().handleCloseMessage(session, close);
    }

    @Override
    public void handleCountMessage(WebSocketSession session, CountRequest count) throws Exception {
        getDelegate().handleCountMessage(session, count);
    }

    @Override
    public void handleUnknownMessage(WebSocketSession session, Request request) throws Exception {
        getDelegate().handleUnknownMessage(session, request);
    }
}
