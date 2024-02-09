package org.tbk.nostr.relay.example.extension.nip11;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties.RelayOptionsProperties;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip11Extension {

    @NonNull
    private final RelayOptionsProperties relayOptions;

    @Bean
    RelayInfoWriterFilter relayInfoWriterFilter() {
        return new RelayInfoWriterFilter(relayOptions);
    }
}
