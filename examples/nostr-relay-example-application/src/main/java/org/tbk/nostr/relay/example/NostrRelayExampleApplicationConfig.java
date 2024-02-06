package org.tbk.nostr.relay.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.impl.*;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;
import org.tbk.nostr.relay.example.nostr.handler.*;
import org.tbk.nostr.relay.example.nostr.support.DefaultUnknownRequestHandler;
import org.tbk.nostr.relay.example.nostr.support.VerifyingEventRequestHandlerDecorator;

@Slf4j
@Configuration(proxyBeanMethods = false)
class NostrRelayExampleApplicationConfig {

    @Bean
    ReqRequestHandler exampleReqRequestHandler() {
        return new ExampleReqRequestHandlerImpl();
    }

    @Bean
    EventRequestHandler exampleEventRequestHandler(EventEntityService eventEntityService) {
        return new ExampleEventRequestHandlerImpl(eventEntityService);
    }

    @Bean
    CloseRequestHandler exampleCloseRequestHandler() {
        return new ExampleCloseRequestHandlerImpl();
    }

    @Bean
    CountRequestHandler exampleCountRequestHandler() {
        return new ExampleCountRequestHandlerImpl();
    }

    @Bean
    @ConditionalOnMissingBean(UnknownRequestHandler.class)
    UnknownRequestHandler defaultUnknownRequestHandler() {
        return new DefaultUnknownRequestHandler();
    }

    @Bean
    NostrWebSocketHandler nostrRelayExampleWebSocketHandler(ReqRequestHandler reqRequestHandler,
                                                            EventRequestHandler eventRequestHandler,
                                                            CloseRequestHandler closeRequestHandler,
                                                            CountRequestHandler countRequestHandler,
                                                            UnknownRequestHandler unknownRequestHandler) {
        return new ExampleNostrWebSocketHandlerImpl(
                reqRequestHandler,
                new VerifyingEventRequestHandlerDecorator(eventRequestHandler),
                closeRequestHandler,
                countRequestHandler,
                unknownRequestHandler
        );
    }
}
