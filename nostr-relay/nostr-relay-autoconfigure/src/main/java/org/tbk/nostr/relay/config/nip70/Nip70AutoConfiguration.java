package org.tbk.nostr.relay.config.nip70;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip70.interceptor.ProtectedEventInterceptor;


@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@ConditionalOnClass(ProtectedEventInterceptor.class)
@RequiredArgsConstructor
public class Nip70AutoConfiguration {

    @Bean
    ProtectedEventInterceptor protectedEventInterceptor() {
        return new ProtectedEventInterceptor();
    }
}
