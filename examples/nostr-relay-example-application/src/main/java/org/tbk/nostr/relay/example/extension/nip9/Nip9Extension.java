package org.tbk.nostr.relay.example.extension.nip9;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip9Extension {

    @NonNull
    private final EventEntityService eventEntityService;

    @Bean
    Nip9EventEntityPostProcessor nip9EventPostProcessor() {
        return new Nip9EventEntityPostProcessor(eventEntityService);
    }
}
