package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

@Slf4j
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
            if (executionChain.applyPreHandle(sessionWrapper, request)) {
                executionChain.applyHandle(sessionWrapper, request, handler);
                executionChain.applyPostHandle(sessionWrapper, request);
            }
        }

        sessionWrapper.tryFlushMessageBuffer();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        handler.afterConnectionClosed(session, status);
    }

    private static class WebSocketSessionWrapper extends WebSocketSessionDecorator implements NostrWebSocketSession {

        private final Queue<WebSocketMessage<?>> messageQueue;

        public WebSocketSessionWrapper(WebSocketSession session) {
            super(session);
            this.messageQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public void sendMessage(WebSocketMessage<?> message) {
            queueMessage(message);
        }


        @Override
        public boolean queueResponse(Response response) {
            return queueMessage(new TextMessage(JsonWriter.toJson(response)));
        }

        @Override
        public void sendResponseImmediately(Response response) throws IOException {
            this.getDelegate().sendMessage(new TextMessage(JsonWriter.toJson(response)));
        }

        private boolean queueMessage(WebSocketMessage<?> message) {
            return this.messageQueue.offer(message);
        }

        private void tryFlushMessageBuffer() throws IOException {
            while (!this.messageQueue.isEmpty()) {
                WebSocketMessage<?> message = this.messageQueue.poll();
                this.getDelegate().sendMessage(message);
            }
        }

        @Override
        public void close() throws IOException {
            this.close(CloseStatus.NORMAL);
        }

        @Override
        public void close(CloseStatus status) throws IOException {
            try {
                tryFlushMessageBuffer();
            } catch (IOException e) {
                try {
                    this.getDelegate().close(status);
                } catch (IOException e2) {
                    log.warn("Swallowed error while closing connection {}: {}", this.getId(), e2.getMessage());
                    // empty on purpose
                }
                throw e;
            }

            this.getDelegate().close(status);
        }
    }
}
