package org.tbk.nostr.relay.config.nip9;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip9.Nip9Support;
import org.tbk.nostr.relay.nip9.interceptor.Nip9RequestHandlerInterceptor;
import org.tbk.nostr.relay.nip9.validation.DeletionEventValidator;

@Slf4j
@ConditionalOnClass(Nip9Support.class)
@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
public class Nip9AutoConfiguration {

    @Bean
    Nip9RequestHandlerInterceptor nip9RequestHandlerInterceptor(Nip9Support support) {
        return new Nip9RequestHandlerInterceptor(support);
    }

    @Bean
    @Order(900_000)
    DeletionEventValidator deletionEventValidator() {
        return new DeletionEventValidator();
    }
}
