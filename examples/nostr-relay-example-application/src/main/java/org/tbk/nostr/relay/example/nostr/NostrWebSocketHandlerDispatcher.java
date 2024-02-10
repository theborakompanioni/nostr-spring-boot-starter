package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.json.JsonReader;

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
        Request request = null;
        try {
            request = JsonReader.fromJson(message.getPayload(), Request.newBuilder());
        } catch (Exception e) {
            handler.handleJsonParseException(session, message, e);
        }

        if (request != null) {
            if (!executionChain.applyPreHandle(session, request)) {
                return;
            }

            executionChain.applyHandle(session, request, handler);

            executionChain.applyPostHandle(session, request);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        handler.afterConnectionClosed(session, status);
    }
}
