package org.tbk.nostr.template.e2e;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;

@Configuration(proxyBeanMethods = false)
class NostrTemplateE2eTestConfiguration {

    @Bean
    RelayUri relay() {
        return RelayUri.parse("wss://relay.primal.net");
    }

    @Bean
    NostrTemplate nostrTemplate(RelayUri relay) {
        return new SimpleNostrTemplate(relay);
    }
}
