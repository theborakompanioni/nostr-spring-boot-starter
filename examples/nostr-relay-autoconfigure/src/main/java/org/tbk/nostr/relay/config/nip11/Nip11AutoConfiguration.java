package org.tbk.nostr.relay.config.nip11;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.config.NostrRelayProperties;
import org.tbk.nostr.relay.nip11.RelayInfoWriterFilter;

@AutoConfiguration
@AutoConfigureAfter(NostrRelayAutoConfiguration.class)
@EnableConfigurationProperties(Nip11Properties.class)
@ConditionalOnClass(RelayInfoWriterFilter.class)
@ConditionalOnProperty(value = "org.tbk.nostr.nip11.enabled")
@ConditionalOnBean(NostrRelayProperties.class)
@RequiredArgsConstructor
class Nip11AutoConfiguration {

    @NonNull
    private final NostrRelayProperties relayProperties;

    @NonNull
    private final Nip11Properties properties;

    @Bean
    RelayInfoWriterFilter relayInfoWriterFilter() {
        return new RelayInfoWriterFilter(relayProperties.getWebsocketPath(), properties.getRelayInfo()
                .toRelayInfoDocument()
                .build());
    }
}
