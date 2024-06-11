package org.tbk.nostr.relay.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.*;
import org.tbk.nostr.relay.handler.*;
import org.tbk.nostr.relay.interceptor.*;
import org.tbk.nostr.relay.support.EventToSubscribers;
import org.tbk.nostr.relay.support.SimpleEventStreamSupport;
import org.tbk.nostr.relay.support.SimpleSubscriptionSupport;
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


    @Bean
    @ConditionalOnMissingBean(SubscriptionSupport.class)
    SimpleSubscriptionSupport simpleSubscriptionSupport() {
        return new SimpleSubscriptionSupport();
    }

    @Bean
    @ConditionalOnMissingBean(EventStreamSupport.class)
    SimpleEventStreamSupport simpleEventStreamSupport() {
        return new SimpleEventStreamSupport();
    }

    @Bean
    @ConditionalOnBean({SubscriptionSupport.class, EventStreamSupport.class})
    EventToSubscribers eventToSubscribers(SubscriptionSupport subscriptionSupport,
                                          EventStreamSupport eventStreamSupport) {
        return new EventToSubscribers(subscriptionSupport, eventStreamSupport);
    }

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
    ValidatingEventInterceptor validatingEventRequestHandlerInterceptor(List<EventValidator> validators, MessageSource messageSource) {
        return new ValidatingEventInterceptor(validators, messageSource);
    }

    @Bean
    @Order(10)
    MaxFilterCountInterceptor maxFilterCountReqRequestHandlerInterceptor() {
        return new MaxFilterCountInterceptor(relayProperties.getMaxFilterCount());
    }

    @Bean
    @Order(20)
    MaxLimitPerFilterInterceptor maxLimitPerFilterReqRequestHandlerInterceptor() {
        return new MaxLimitPerFilterInterceptor(relayProperties.getMaxLimitPerFilter());
    }

    @Bean
    @ConditionalOnBean(SubscriptionSupport.class)
    SubscriptionInterceptor subscriptionHandlerInterceptor(SubscriptionSupport support) {
        return new SubscriptionInterceptor(support);
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
    @ConditionalOnBean(NostrSupport.class)
    @ConditionalOnMissingBean(EventRequestHandler.class)
    DefaultEventRequestHandler defaultEventRequestHandler(NostrSupport support) {
        return new DefaultEventRequestHandler(support);
    }

    @Bean
    @ConditionalOnMissingBean(CloseRequestHandler.class)
    DefaultCloseRequestHandler defaultCloseRequestHandler() {
        return new DefaultCloseRequestHandler();
    }


    @Bean
    @ConditionalOnMissingBean(AuthRequestHandler.class)
    DefaultAuthRequestHandler defaultAuthRequestHandler() {
        return new DefaultAuthRequestHandler();
    }

    @Bean
    @ConditionalOnMissingBean(CountRequestHandler.class)
    DefaultCountRequestHandler defaultCountRequestHandler() {
        return new DefaultCountRequestHandler();
    }

    @Bean
    @ConditionalOnMissingBean(UnknownRequestHandler.class)
    DefaultUnknownRequestHandler defaultUnknownRequestHandler() {
        return new DefaultUnknownRequestHandler();
    }

    @Bean
    @ConditionalOnMissingBean(ParseErrorHandler.class)
    DefaultParseErrorHandler defaultParseErrorHandler() {
        return new DefaultParseErrorHandler();
    }
    // request handler - end

    @Bean
    @ConditionalOnMissingBean(NostrWebSocketHandler.class)
    NostrRequestHandlerSupport nostrWebSocketHandler(ReqRequestHandler reqRequestHandler,
                                                     EventRequestHandler eventRequestHandler,
                                                     CloseRequestHandler closeRequestHandler,
                                                     CountRequestHandler countRequestHandler,
                                                     AuthRequestHandler authRequestHandler,
                                                     UnknownRequestHandler unknownRequestHandler,
                                                     ParseErrorHandler parseErrorHandler) {
        return new NostrRequestHandlerSupport(
                reqRequestHandler,
                eventRequestHandler,
                closeRequestHandler,
                countRequestHandler,
                authRequestHandler,
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
