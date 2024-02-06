package org.tbk.nostr.relay.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;
import org.tbk.nostr.relay.example.nostr.VerifyingNostrWebSocketHandlerDecorator;

@Slf4j
@Configuration(proxyBeanMethods = false)
class NostrRelayExampleApplicationConfig {

    @Bean
    NostrWebSocketHandler nostrRelayExampleWebSocketHandler(EventEntityService eventEntityService) {
        return new VerifyingNostrWebSocketHandlerDecorator(
                new NostrRelayExampleWebSocketHandler(eventEntityService)
        );
    }

    @Bean
    @ConditionalOnWebApplication
    ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(8192);
        container.setMaxBinaryMessageBufferSize(8192);
        return container;
    }
}
