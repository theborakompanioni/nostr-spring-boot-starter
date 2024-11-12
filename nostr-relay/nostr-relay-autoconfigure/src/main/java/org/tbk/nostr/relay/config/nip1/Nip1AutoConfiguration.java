package org.tbk.nostr.relay.config.nip1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip1.Nip1Support;
import org.tbk.nostr.relay.nip1.interceptor.EphemeralEventInterceptor;
import org.tbk.nostr.relay.nip1.interceptor.ReplaceableEventInterceptor;
import org.tbk.nostr.relay.nip1.validation.AddressableEventValidator;

@Slf4j
@ConditionalOnClass(Nip1Support.class)
@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
public class Nip1AutoConfiguration {

    // validators
    @Bean
    @Order(20)
    AddressableEventValidator addressableEventValidator() {
        return new AddressableEventValidator();
    }
    // validators - end

    @Bean
    @ConditionalOnBean(Nip1Support.class)
    ReplaceableEventInterceptor replaceableEventRequestHandlerInterceptor(Nip1Support support) {
        return new ReplaceableEventInterceptor(support);
    }

    @Bean
    @ConditionalOnBean(Nip1Support.class)
    EphemeralEventInterceptor ephemeralEventRequestHandlerInterceptor(Nip1Support support) {
        return new EphemeralEventInterceptor(support);
    }
}
