package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.validating.DefaultEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

import java.util.List;

@EnableWebSocket
@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrWebSocketConfiguration {

    @NonNull
    private final NostrWebSocketHandler nostrWebSocketHandler;

    @Bean
    NostrRequestHandlerExecutionChain nostrRequestHandlerExecutionChain(List<NostrRequestHandlerInterceptor> interceptors) {
        return new NostrRequestHandlerExecutionChain(interceptors);
    }

    @Bean
    NostrWebSocketHandlerDispatcher nostrWebSocketHandlerDispatcher(NostrRequestHandlerExecutionChain executionChain) {
        return new NostrWebSocketHandlerDispatcher(executionChain, nostrWebSocketHandler);
    }

    @Bean
    DefaultEventValidator DefaultEventValidator() {
        return new DefaultEventValidator();
    }

    @Bean
    ValidatingEventRequestHandlerInterceptor validatingEventRequestHandlerInterceptor(List<EventValidator> validators) {
        return new ValidatingEventRequestHandlerInterceptor(validators);
    }
}
