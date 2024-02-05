package org.tbk.nostr.relay.example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.AbstractNostrWebSocketHandler;

import java.io.IOException;

@ConditionalOnWebApplication
@EnableWebSocket
@Configuration(proxyBeanMethods = false)
class NostrRelayExampleApplicationWebSocketConfigurer implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LoggingWebSocketHandlerDecorator(new NostrWebSocketHandler()), "/");
    }

    private static class NostrWebSocketHandler extends AbstractNostrWebSocketHandler {
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            try {
                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setNotice(NoticeResponse.newBuilder()
                                .setMessage("GM")
                                .build())
                        .build())));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {

        }

        @Override
        protected void handleEventMessage(WebSocketSession session, EventRequest event) {

        }

        @Override
        protected void handleReqMessage(WebSocketSession session, ReqRequest req) {

        }

        @Override
        protected void handleCloseMessage(WebSocketSession session, CloseRequest close) {

        }

        @Override
        protected void handleCountMessage(WebSocketSession session, CountRequest count) {

        }

        @Override
        protected void handleUnknownMessage(WebSocketSession session, Request request) {

        }
    }
}