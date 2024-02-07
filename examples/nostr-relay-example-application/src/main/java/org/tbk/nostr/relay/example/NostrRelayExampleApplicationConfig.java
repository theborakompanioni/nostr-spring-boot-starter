package org.tbk.nostr.relay.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties.RelayOptionsProperties;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.impl.*;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;
import org.tbk.nostr.relay.example.nostr.handler.*;
import org.tbk.nostr.relay.example.nostr.support.DefaultUnknownRequestHandler;
import org.tbk.nostr.relay.example.nostr.support.MaxFilterCountReqRequestHandlerDecorator;
import org.tbk.nostr.relay.example.nostr.support.MaxLimitPerFilterReqRequestHandlerDecorator;
import org.tbk.nostr.relay.example.nostr.support.VerifyingEventRequestHandlerDecorator;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NostrRelayExampleApplicationProperties.class)
@RequiredArgsConstructor
class NostrRelayExampleApplicationConfig {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Bean
    RelayOptionsProperties relayOptions() {
        return properties.getRelayOptions();
    }

    @Bean
    @ConditionalOnProperty("org.tbk.nostr.relay.example.identity.mnemonics")
    Signer serverSigner() {
        return properties.getIdentity()
                .map(NostrRelayExampleApplicationProperties.IdentityProperties::getSeed)
                .map(MoreIdentities::fromSeed)
                .map(SimpleSigner::fromPrivateKey)
                .orElseThrow();
    }

    @Bean
    ReqRequestHandler exampleReqRequestHandler(EventEntityService eventEntityService,
                                               RelayOptionsProperties relayOptions) {
        return new MaxFilterCountReqRequestHandlerDecorator(
                new MaxLimitPerFilterReqRequestHandlerDecorator(
                        new ExampleReqRequestHandlerImpl(eventEntityService),
                        relayOptions.getMaxLimitPerFilter()
                ),
                relayOptions.getMaxFilterCount()
        );
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
                properties,
                reqRequestHandler,
                new VerifyingEventRequestHandlerDecorator(eventRequestHandler),
                closeRequestHandler,
                countRequestHandler,
                unknownRequestHandler
        );
    }
}
