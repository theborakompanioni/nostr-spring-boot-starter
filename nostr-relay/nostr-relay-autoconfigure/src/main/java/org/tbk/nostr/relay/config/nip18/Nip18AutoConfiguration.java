package org.tbk.nostr.relay.config.nip18;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.relay.config.NostrRelayAutoConfiguration;
import org.tbk.nostr.relay.nip18.validation.RepostEventValidator;
import org.tbk.nostr.relay.validation.DefaultEventValidator;

@AutoConfiguration
@AutoConfigureBefore(NostrRelayAutoConfiguration.class)
@ConditionalOnClass(RepostEventValidator.class)
@RequiredArgsConstructor
class Nip18AutoConfiguration {

    @Bean
    RepostEventValidator repostEventValidator(DefaultEventValidator defaultEventValidator) {
        return new RepostEventValidator(defaultEventValidator);
    }
}
