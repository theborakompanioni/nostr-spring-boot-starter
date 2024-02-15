package org.tbk.nostr.relay.example.nostr.nip1;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.example.nostr.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.example.nostr.extension.nip1.EphemeralEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.extension.nip1.Nip1Support;
import org.tbk.nostr.relay.example.nostr.extension.nip1.ReplaceableEventRequestHandlerInterceptor;
import org.tbk.nostr.relay.example.nostr.extension.nip1.ReplaceableEventValidator;

@Slf4j
@ConditionalOnClass(Nip1Support.class)
@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@RequiredArgsConstructor
public class Nip1AutoConfiguration {

    // validators
    @Bean
    @Order(20)
    ReplaceableEventValidator replaceableEventValidator() {
        return new ReplaceableEventValidator();
    }
    // validators - end

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
