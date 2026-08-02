package org.tbk.nostr.autoconfigure.template;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.template.SimpleNostrTemplate;

@AutoConfiguration
@EnableConfigurationProperties(NostrTemplateProperties.class)
@ConditionalOnProperty(value = "org.tbk.nostr.template.relay-uri")
@ConditionalOnClass(NostrTemplate.class)
@RequiredArgsConstructor
public class NostrTemplateAutoConfiguration {

    @NonNull
    private final NostrTemplateProperties nostrTemplateProperties;

    @Bean
    @ConditionalOnMissingBean
    NostrTemplate nostrTemplate() {
        RelayUri relayUri = RelayUri.of(nostrTemplateProperties.getRelayUri());
        return new SimpleNostrTemplate(relayUri);
    }
}
