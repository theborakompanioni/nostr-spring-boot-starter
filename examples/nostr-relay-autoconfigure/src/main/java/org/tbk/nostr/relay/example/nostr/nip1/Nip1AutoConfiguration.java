package org.tbk.nostr.relay.example.nostr.nip1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.example.nostr.extension.nip1.EphemeralEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.extension.nip1.Nip1Support;
import org.tbk.nostr.relay.example.nostr.extension.nip1.ReplaceableEventRequestHandlerInterceptor;

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
}
