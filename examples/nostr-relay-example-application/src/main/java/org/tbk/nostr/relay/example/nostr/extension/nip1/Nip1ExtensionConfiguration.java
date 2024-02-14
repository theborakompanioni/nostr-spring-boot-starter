package org.tbk.nostr.relay.example.nostr.extension.nip1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip1ExtensionConfiguration {

    @Bean
    ReplaceableEventRequestHandlerInterceptor replaceableEventRequestHandlerInterceptor(Nip1Support support) {
        return new ReplaceableEventRequestHandlerInterceptor(support);
    }

    @Bean
    EphemeralEventRequestHandlerInterceptor ephemeralEventRequestHandlerInterceptor(Nip1Support support) {
        return new EphemeralEventRequestHandlerInterceptor(support);
    }

    @Bean
    @Order(1)
    ReplaceableEventValidator replaceableEventValidator() {
        return new ReplaceableEventValidator();
    }

}
