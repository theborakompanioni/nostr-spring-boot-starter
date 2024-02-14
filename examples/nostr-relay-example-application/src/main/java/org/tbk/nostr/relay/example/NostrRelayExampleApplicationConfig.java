package org.tbk.nostr.relay.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;
import org.tbk.nostr.relay.example.impl.*;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketHandler;
import org.tbk.nostr.relay.example.nostr.extension.nip1.DefaultReqRequestHandler;
import org.tbk.nostr.relay.example.nostr.extension.nip1.Nip1Support;
import org.tbk.nostr.relay.example.nostr.handler.*;
import org.tbk.nostr.relay.example.nostr.support.DefaultUnknownRequestHandler;

@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(NostrRelayExampleApplicationProperties.class)
@RequiredArgsConstructor
class NostrRelayExampleApplicationConfig {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Bean
    @ConditionalOnProperty("org.tbk.nostr.example.relay.identity.mnemonics")
    Signer serverSigner() {
        return properties.getIdentity()
                .map(NostrRelayExampleApplicationProperties.IdentityProperties::getSeed)
                .map(MoreIdentities::fromSeed)
                .map(SimpleSigner::fromPrivateKey)
                .orElseThrow();
    }

    @Bean
    NipSupportService nipSupportService(EventEntityService eventEntityService, ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor) {
        return new NipSupportService(eventEntityService, asyncThreadPoolTaskExecutor);
    }

    // TODO: move to own autoconfigure module
    @Bean
    @ConditionalOnMissingBean(ReqRequestHandler.class)
    DefaultReqRequestHandler defaultReqRequestHandler(Nip1Support support) {
        return new DefaultReqRequestHandler(support);
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
                this.properties,
                reqRequestHandler,
                eventRequestHandler,
                closeRequestHandler,
                countRequestHandler,
                unknownRequestHandler
        );
    }
}
