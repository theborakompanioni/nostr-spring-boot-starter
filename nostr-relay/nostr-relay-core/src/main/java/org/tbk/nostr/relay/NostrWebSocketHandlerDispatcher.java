package org.tbk.nostr.relay;

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
import org.tbk.nostr.relay.handler.ConnectionClosedHandler;
import org.tbk.nostr.relay.handler.ConnectionEstablishedHandler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.Objects.requireNonNull;

@Slf4j
public class NostrWebSocketHandlerDispatcher extends TextWebSocketHandler {

    private final NostrRequestHandlerExecutionChain executionChain;

    private final NostrWebSocketHandler handler;

    private final List<ConnectionEstablishedHandler> connectionEstablishedHandler;

    private final List<ConnectionClosedHandler> connectionClosedHandler;

    public NostrWebSocketHandlerDispatcher(NostrRequestHandlerExecutionChain executionChain,
                                           NostrWebSocketHandler handler,
                                           List<ConnectionEstablishedHandler> connectionEstablishedHandler,
                                           List<ConnectionClosedHandler> connectionClosedHandler) {
        this.executionChain = requireNonNull(executionChain);
        this.handler = requireNonNull(handler);
        this.connectionEstablishedHandler = Collections.unmodifiableList(requireNonNull(connectionEstablishedHandler));
        this.connectionClosedHandler = Collections.unmodifiableList(requireNonNull(connectionClosedHandler));
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        if (log.isTraceEnabled()) {
            log.trace("WebSocket connection established: {}", session.getId());
        }

        WebSocketSessionWrapper sessionWrapper = new WebSocketSessionWrapper(session);

        for (ConnectionEstablishedHandler ceh : this.connectionEstablishedHandler) {
            ceh.afterConnectionEstablished(sessionWrapper);
        }

        sessionWrapper.tryFlushMessageBuffer();
    }

    @Override
    protected final void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        WebSocketSessionWrapper sessionWrapper = new WebSocketSessionWrapper(session);

        Request request = null;
        try {
            request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        } catch (Exception e) {
            handler.handleParseError(sessionWrapper, message, e);
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
        if (log.isTraceEnabled()) {
            log.trace("WebSocket connection closed ({}): {}", status.getCode(), session.getId());
        }

        WebSocketSessionWrapper sessionWrapper = new WebSocketSessionWrapper(session);
        for (ConnectionClosedHandler cch : this.connectionClosedHandler) {
            cch.afterConnectionClosed(sessionWrapper, status);
        }
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
