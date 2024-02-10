package org.tbk.nostr.relay.example.extension.nip11;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(Nip11ExtensionProperties.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip11.enabled")
@RequiredArgsConstructor
class Nip11Extension {

    @NonNull
    private final Nip11ExtensionProperties properties;

    @Bean
    RelayInfoWriterFilter relayInfoWriterFilter() {
        return new RelayInfoWriterFilter(properties.getPath(), properties.getRelayInfo().toRelayInfoDocument().build());
    }
}
