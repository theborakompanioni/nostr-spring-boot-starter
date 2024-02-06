package org.tbk.nostr.relay.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;

@ConditionalOnWebApplication
@EnableWebSocket
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrRelayExampleWebSocketConfigurer implements WebSocketConfigurer {

    @NonNull
    private final NostrWebSocketHandler nostrRelayExampleWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LoggingWebSocketHandlerDecorator(nostrRelayExampleWebSocketHandler), "/")
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
