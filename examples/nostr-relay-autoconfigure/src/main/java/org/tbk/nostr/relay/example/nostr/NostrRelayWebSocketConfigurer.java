package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.LoggingWebSocketHandlerDecorator;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketConfigurer.class)
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrRelayWebSocketConfigurer implements WebSocketConfigurer {

    @NonNull
    private final NostrRelayProperties relayProperties;

    @NonNull
    private final NostrWebSocketHandlerDispatcher nostrWebSocketHandlerDispatcher;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new LoggingWebSocketHandlerDecorator(nostrWebSocketHandlerDispatcher), relayProperties.getWebsocketPath())
                .addInterceptors(new HttpSessionHandshakeInterceptor())
                .setAllowedOrigins("*");
    }

    @Bean
    ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}
