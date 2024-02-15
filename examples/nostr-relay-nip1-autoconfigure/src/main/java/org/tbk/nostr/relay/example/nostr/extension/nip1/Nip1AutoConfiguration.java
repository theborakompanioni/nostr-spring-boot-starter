package org.tbk.nostr.relay.example.nostr.extension.nip1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;

@Slf4j
@ConditionalOnClass(Nip1Support.class)
@AutoConfiguration
@RequiredArgsConstructor
class Nip1AutoConfiguration {

    @Bean
    @ConditionalOnBean(Nip1Support.class)
    ReplaceableEventRequestHandlerInterceptor replaceableEventRequestHandlerInterceptor(Nip1Support support) {
        return new ReplaceableEventRequestHandlerInterceptor(support);
    }

    @Bean
    @ConditionalOnBean(Nip1Support.class)
    EphemeralEventRequestHandlerInterceptor ephemeralEventRequestHandlerInterceptor(Nip1Support support) {
        return new EphemeralEventRequestHandlerInterceptor(support);
    }

    @Bean
    @Order(1)
    @ConditionalOnBean(Nip1Support.class)
    ReplaceableEventValidator replaceableEventValidator() {
        return new ReplaceableEventValidator();
    }

    // request handler
    @Bean
    @ConditionalOnBean(Nip1Support.class)
    //@ConditionalOnMissingBean(ReqRequestHandler.class)
    DefaultReqRequestHandler defaultReqRequestHandler(Nip1Support support) {
        return new DefaultReqRequestHandler(support);
    }
    // request handler - end
}
