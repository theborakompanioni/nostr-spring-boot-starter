package org.tbk.nostr.relay.example.nostr;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.example.nostr.handler.*;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxFilterCountReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.MaxLimitPerFilterReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.validation.CreatedAtLimitEventValidator;
import org.tbk.nostr.relay.example.nostr.validation.DefaultEventValidator;
import org.tbk.nostr.relay.example.nostr.validation.EventValidator;

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
    MaxFilterCountReqRequestHandlerInterceptor maxFilterCountReqRequestHandlerInterceptor() {
        return new MaxFilterCountReqRequestHandlerInterceptor(relayProperties.getMaxFilterCount());
    }

    @Bean
    @Order(20)
    MaxLimitPerFilterReqRequestHandlerInterceptor maxLimitPerFilterReqRequestHandlerInterceptor() {
        return new MaxLimitPerFilterReqRequestHandlerInterceptor(relayProperties.getMaxLimitPerFilter());
    }
    // interceptors - end

    // request handler
    @Bean
    @ConditionalOnBean(NostrSupport.class)
    @ConditionalOnMissingBean(ReqRequestHandler.class)
    DefaultReqRequestHandler defaultReqRequestHandler(NostrSupport support) {
        return new DefaultReqRequestHandler(support);
    }

    @Bean
    @ConditionalOnMissingBean(UnknownRequestHandler.class)
    UnknownRequestHandler defaultUnknownRequestHandler() {
        return new DefaultUnknownRequestHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ParseErrorHandler.class)
    ParseErrorHandler defaultParseErrorHandler() {
        return new DefaultParseErrorHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionEstablishedHandler.class)
    ConnectionEstablishedHandler defaultConnectionEstablishedHandler() {
        return new DefaultConnectionEstablishedHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ConnectionClosedHandler.class)
    ConnectionClosedHandler defaultConnectionClosedHandler() {
        return new DefaultConnectionClosedHandler();
    }
    // request handler - end

    @Bean
    @ConditionalOnMissingBean(NostrWebSocketHandler.class)
    NostrWebSocketHandler nostrWebSocketHandler(ConnectionEstablishedHandler connectionEstablishedHandler,
                                                ConnectionClosedHandler connectionClosedHandler,
                                                ReqRequestHandler reqRequestHandler,
                                                EventRequestHandler eventRequestHandler,
                                                CloseRequestHandler closeRequestHandler,
                                                CountRequestHandler countRequestHandler,
                                                UnknownRequestHandler unknownRequestHandler,
                                                ParseErrorHandler parseErrorHandler) {
        return new NostrRequestHandlerSupport(
                connectionEstablishedHandler,
                connectionClosedHandler,
                reqRequestHandler,
                eventRequestHandler,
                closeRequestHandler,
                countRequestHandler,
                unknownRequestHandler,
                parseErrorHandler
        );
    }

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
