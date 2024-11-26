package org.tbk.nostr.relay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.handler.ConnectionClosedHandler;
import org.tbk.nostr.relay.handler.ConnectionEstablishedHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

        NostrRequestContextImpl context = new NostrRequestContextImpl(sessionWrapper);

        Request request = null;
        try {
            request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        } catch (Exception e) {
            handler.handleParseError(context, message, e);
        }

        if (request != null) {
            if (executionChain.applyPreHandle(context, request)) {
                executionChain.applyHandle(context, request, handler);
                executionChain.applyPostHandle(context, request);
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

    private static class NostrRequestContextImpl implements NostrRequestContext {

        private final NostrWebSocketSession session;

        private Event event;

        NostrRequestContextImpl(NostrWebSocketSession session) {
            this.session = requireNonNull(session);
        }

        @Override
        public NostrWebSocketSession getSession() {
            return this.session;
        }

        @Override
        public boolean add(Response response) {
            return this.session.queueResponse(response);
        }

        @Override
        public void setHandledEvent(Event event) {
            this.event = event;
        }

        @Override
        public Optional<Event> getHandledEvent() {
            return Optional.ofNullable(event);
        }

        @Override
        public Optional<Principal> getAuthentication() {
            return getSession().getAuthentication();
        }

        @Override
        public void setAuthentication(Principal principal) {
            getSession().setAuthentication(principal);
        }

        @Override
        public Optional<String> getAuthenticationChallenge() {
            return getSession().getAuthenticationChallenge();
        }

        @Override
        public void setAuthenticationChallenge(String challenge) {
            getSession().setAuthenticationChallenge(challenge);
        }
    }

    private static class WebSocketSessionWrapper extends WebSocketSessionDecorator implements NostrWebSocketSession {

        private final SessionId sessionId;

        private final Queue<WebSocketMessage<?>> messageQueue;

        public WebSocketSessionWrapper(WebSocketSession session) {
            super(session);
            this.sessionId = new SessionId(session.getId());
            this.messageQueue = new LinkedBlockingQueue<>();
        }

        @Override
        public SessionId getSessionId() {
            return this.sessionId;
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

        @Override
        public Optional<Principal> getAuthentication() {
            return Optional.ofNullable(getAttributes().get("nip42_auth"))
                    .map(Principal.class::cast);
        }

        @Override
        public void setAuthentication(Principal principal) {
            if (principal == null) {
                getAttributes().remove("nip42_auth");
            } else {
                getAttributes().put("nip42_auth", principal);
            }
        }

        @Override
        public Optional<String> getAuthenticationChallenge() {
            return Optional.ofNullable(getAttributes().get("nip42_challenge")).map(String::valueOf);
        }

        @Override
        public void setAuthenticationChallenge(String challenge) {
            if (challenge == null) {
                getAttributes().remove("nip42_challenge");
            } else {
                getAttributes().put("nip42_challenge", challenge);
            }
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
                }
                throw e;
            }

            this.getDelegate().close(status);
        }
    }
}
