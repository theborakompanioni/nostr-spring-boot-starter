package org.tbk.nostr.relay.config.nip40;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip40.Nip40Support;
import org.tbk.nostr.relay.nip40.interceptor.ExpiringEventHandlerInterceptor;


@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@ConditionalOnClass(Nip40Support.class)
@ConditionalOnBean(Nip40Support.class)
public class Nip40AutoConfiguration {

    @Bean
    ExpiringEventHandlerInterceptor expiringEventHandlerInterceptor(Nip40Support support) {
        return new ExpiringEventHandlerInterceptor(support);
    }
}
