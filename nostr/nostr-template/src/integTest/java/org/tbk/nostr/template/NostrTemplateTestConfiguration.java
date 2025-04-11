package org.tbk.nostr.template;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;

@Configuration(proxyBeanMethods = false)
class NostrTemplateTestConfiguration {

    @Bean
    RelayUri relay() {
        return RelayUri.parse("ws://localhost:7000");
    }

    @Bean
    NostrTemplate nostrTemplate(RelayUri relay) {
        return new SimpleNostrTemplate(relay);
    }
}
