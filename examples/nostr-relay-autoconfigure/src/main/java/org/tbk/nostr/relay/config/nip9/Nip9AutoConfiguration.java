package org.tbk.nostr.relay.config.nip9;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip9.Nip9Support;
import org.tbk.nostr.relay.nip9.interceptor.DeletionEventHandlerInterceptor;
import org.tbk.nostr.relay.nip9.validation.DeletionEventValidator;

@Slf4j
@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@ConditionalOnClass(Nip9Support.class)
@ConditionalOnBean(Nip9Support.class)
public class Nip9AutoConfiguration {

    @Bean
    DeletionEventHandlerInterceptor nip9RequestHandlerInterceptor(Nip9Support support) {
        return new DeletionEventHandlerInterceptor(support);
    }

    @Bean
    @Order(9_000_000)
    DeletionEventValidator deletionEventValidator() {
        return new DeletionEventValidator();
    }
}
