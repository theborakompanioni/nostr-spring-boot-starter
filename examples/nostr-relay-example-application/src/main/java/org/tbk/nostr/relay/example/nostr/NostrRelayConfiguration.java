package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxFilterCountReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxLimitPerFilterReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.validating.DefaultEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

import java.util.List;

@EnableWebSocket
@ConditionalOnWebApplication
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NostrRelayProperties.class)
@RequiredArgsConstructor
class NostrRelayConfiguration {
    @NonNull
    private final NostrRelayProperties relayProperties;

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
    DefaultEventValidator defaultEventValidator() {
        return new DefaultEventValidator();
    }

    @Bean
    ValidatingEventRequestHandlerInterceptor validatingEventRequestHandlerInterceptor(List<EventValidator> validators) {
        return new ValidatingEventRequestHandlerInterceptor(validators);
    }

    @Bean
    MaxLimitPerFilterReqRequestHandlerInterceptor maxLimitPerFilterReqRequestHandlerInterceptor() {
        return new MaxLimitPerFilterReqRequestHandlerInterceptor(relayProperties.getMaxLimitPerFilter());
    }

    @Bean
    MaxFilterCountReqRequestHandlerInterceptor maxFilterCountReqRequestHandlerInterceptor() {
        return new MaxFilterCountReqRequestHandlerInterceptor(relayProperties.getMaxFilterCount());
    }
}
