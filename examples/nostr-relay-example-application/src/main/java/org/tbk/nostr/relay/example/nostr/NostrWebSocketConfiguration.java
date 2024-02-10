package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrWebSocketConfiguration {

    @NonNull
    private final NostrWebSocketHandler nostrWebSocketHandler;

    @Bean
    NostrRequestHandlerExecutionChain nostrRequestHandlerExecutionChain() {
        return new NostrRequestHandlerExecutionChain();
    }

    @Bean
    NostrWebSocketHandlerDispatcher nostrWebSocketHandlerDispatcher(NostrRequestHandlerExecutionChain executionChain) {
        return new NostrWebSocketHandlerDispatcher(executionChain, nostrWebSocketHandler);
    }
}
