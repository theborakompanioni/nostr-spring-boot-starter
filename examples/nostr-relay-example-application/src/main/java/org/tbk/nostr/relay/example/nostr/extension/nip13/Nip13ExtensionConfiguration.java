package org.tbk.nostr.relay.example.nostr.extension.nip13;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Nip13ExtensionProperties.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip13.enabled")
@RequiredArgsConstructor
class Nip13ExtensionConfiguration {

    @NonNull
    private final Nip13ExtensionProperties properties;

    @Bean
    PowEventValidator powEventValidator() {
        return new PowEventValidator(this.properties);
    }
}
