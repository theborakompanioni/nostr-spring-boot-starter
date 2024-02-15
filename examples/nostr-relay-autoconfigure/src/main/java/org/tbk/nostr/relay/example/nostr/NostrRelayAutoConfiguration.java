package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.example.nostr.handler.DefaultUnknownRequestHandler;
import org.tbk.nostr.relay.example.nostr.handler.UnknownRequestHandler;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxFilterCountReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxLimitPerFilterReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.validating.CreatedAtLimitEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.DefaultEventValidator;
import org.tbk.nostr.relay.example.nostr.validating.EventValidator;

import java.util.List;

@ConditionalOnWebApplication
@AutoConfiguration
@EnableConfigurationProperties(NostrRelayProperties.class)
@RequiredArgsConstructor
class NostrRelayAutoConfiguration {

    @NonNull
    private final NostrRelayProperties relayProperties;

    // validators
    @Bean
    @Order(0)
    DefaultEventValidator defaultEventValidator() {
        return new DefaultEventValidator();
    }

    @Bean
    @Order(10)
    CreatedAtLimitEventValidator createdAtLimitEventValidator() {
        return new CreatedAtLimitEventValidator(relayProperties.getCreatedAtLowerLimit(), relayProperties.getCreatedAtUpperLimit());
    }
    // validators - end

    // interceptors
    @Bean
    @Order(0)
    ValidatingEventRequestHandlerInterceptor validatingEventRequestHandlerInterceptor(List<EventValidator> validators) {
        return new ValidatingEventRequestHandlerInterceptor(validators);
    }

    @Bean
    @Order(10)
    MaxLimitPerFilterReqRequestHandlerInterceptor maxLimitPerFilterReqRequestHandlerInterceptor() {
        return new MaxLimitPerFilterReqRequestHandlerInterceptor(relayProperties.getMaxLimitPerFilter());
    }

    @Bean
    @Order(10)
    MaxFilterCountReqRequestHandlerInterceptor maxFilterCountReqRequestHandlerInterceptor() {
        return new MaxFilterCountReqRequestHandlerInterceptor(relayProperties.getMaxFilterCount());
    }
    // interceptors - end

    // request handler
    @Bean
    //@ConditionalOnMissingBean(UnknownRequestHandler.class)
    UnknownRequestHandler defaultUnknownRequestHandler() {
        return new DefaultUnknownRequestHandler();
    }
    // request handler - end


    @Bean
    NostrRequestHandlerExecutionChain nostrRequestHandlerExecutionChain(List<RequestHandlerInterceptor> interceptors) {
        return new NostrRequestHandlerExecutionChain(interceptors);
    }

    @Bean
    NostrWebSocketHandlerDispatcher nostrWebSocketHandlerDispatcher(NostrRequestHandlerExecutionChain executionChain,
                                                                    NostrWebSocketHandler nostrWebSocketHandler) {
        return new NostrWebSocketHandlerDispatcher(executionChain, nostrWebSocketHandler);
    }
}
