package org.tbk.nostr.template;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrTemplateTestConfiguration {

    @Bean
    RelayUri relay() {
        return RelayUri.of(URI.create("ws://localhost:7000"));
    }

    @Bean
    NostrTemplate nostrTemplate(RelayUri relay) {
        return new SimpleNostrTemplate(relay);
    }
}
