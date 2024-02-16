package org.tbk.nostr.relay.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.*;
import org.tbk.nostr.relay.handler.*;
import org.tbk.nostr.relay.interceptor.MaxFilterCountReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.interceptor.MaxLimitPerFilterReqRequestHandlerInterceptor;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;
import org.tbk.nostr.relay.interceptor.ValidatingEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.validation.CreatedAtLimitEventValidator;
import org.tbk.nostr.relay.validation.DefaultEventValidator;
import org.tbk.nostr.relay.validation.EventValidator;

import java.util.List;

@ConditionalOnWebApplication
@AutoConfiguration
@EnableConfigurationProperties(NostrRelayProperties.class)
@RequiredArgsConstructor
public class NostrRelayAutoConfiguration {

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
    ReqRequestHandler defaultReqRequestHandler(NostrSupport support) {
        return new DefaultReqRequestHandler(support);
    }

    @Bean
    @ConditionalOnBean(NostrSupport.class)
    @ConditionalOnMissingBean(EventRequestHandler.class)
    EventRequestHandler defaultEventRequestHandler(NostrSupport support) {
        return new DefaultEventRequestHandler(support);
    }

    @Bean
    @ConditionalOnMissingBean(CountRequestHandler.class)
    CountRequestHandler defaultCountRequestHandler() {
        return new DefaultCountRequestHandler();
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
    // request handler - end

    @Bean
    @ConditionalOnMissingBean(NostrWebSocketHandler.class)
    NostrWebSocketHandler nostrWebSocketHandler(ReqRequestHandler reqRequestHandler,
                                                EventRequestHandler eventRequestHandler,
                                                CloseRequestHandler closeRequestHandler,
                                                CountRequestHandler countRequestHandler,
                                                UnknownRequestHandler unknownRequestHandler,
                                                ParseErrorHandler parseErrorHandler) {
        return new NostrRequestHandlerSupport(
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
                                                                    NostrWebSocketHandler nostrWebSocketHandler,
                                                                    List<ConnectionEstablishedHandler> connectionEstablishedHandler,
                                                                    List<ConnectionClosedHandler> connectionClosedHandler) {
        return new NostrWebSocketHandlerDispatcher(
                executionChain,
                nostrWebSocketHandler,
                connectionEstablishedHandler,
                connectionClosedHandler
        );
    }
}
