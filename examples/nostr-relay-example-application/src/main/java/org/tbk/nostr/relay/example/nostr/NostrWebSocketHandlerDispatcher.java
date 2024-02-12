package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.json.JsonReader;

import java.io.IOException;

@RequiredArgsConstructor
public class NostrWebSocketHandlerDispatcher extends TextWebSocketHandler {

    @NonNull
    private final NostrRequestHandlerExecutionChain executionChain;

    @NonNull
    private final NostrWebSocketHandler handler;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        handler.afterConnectionEstablished(session);
    }

    @Override
    protected final void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketSessionWrapper sessionWrapper = new WebSocketSessionWrapper(session);

        Request request = null;
        try {
            request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        } catch (Exception e) {
            handler.handleJsonParseException(sessionWrapper, message, e);
        }

        if (request != null) {
            if (!executionChain.applyPreHandle(sessionWrapper, request)) {
                return;
            }

            executionChain.applyHandle(sessionWrapper, request, handler);

            executionChain.applyPostHandle(sessionWrapper, request);
        }

        sessionWrapper.tryFlushMessageBuffer();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        handler.afterConnectionClosed(session, status);
    }

    private static class WebSocketSessionWrapper extends WebSocketSessionDecorator {

        public WebSocketSessionWrapper(WebSocketSession session) {
            super(session);
        }

        @Override
        public void setTextMessageSizeLimit(int messageSizeLimit) {
            throw new UnsupportedOperationException("'setTextMessageSizeLimit' has been disabled on purpose. Do it in method 'afterConnectionEstablished'.");
        }

        @Override
        public void setBinaryMessageSizeLimit(int messageSizeLimit) {
            throw new UnsupportedOperationException("'setBinaryMessageSizeLimit' has been disabled on purpose. Do it in method 'afterConnectionEstablished'.");
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) throws IOException {
            // TODO: delay sending message
            // e.g. take a look at ConcurrentWebSocketSessionDecorator
            this.getDelegate().sendMessage(message);
        }

        public void tryFlushMessageBuffer() {
            // TODO: try flushing all messages in the buffer here
            // e.g. take a look at ConcurrentWebSocketSessionDecorator
        }

        @Override
        public void close() throws IOException {
            tryFlushMessageBuffer();
            this.getDelegate().close();
        }

        @Override
        public void close(CloseStatus status) throws IOException {
            tryFlushMessageBuffer();
            this.getDelegate().close(status);
        }
    }
}
