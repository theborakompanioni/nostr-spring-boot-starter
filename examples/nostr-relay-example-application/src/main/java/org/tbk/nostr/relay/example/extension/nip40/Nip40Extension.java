package org.tbk.nostr.relay.example.extension.nip40;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.relay.example.domain.event.EventEntityService;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class Nip40Extension {

    @NonNull
    private final EventEntityService eventEntityService;

    @Bean
    Nip40EventEntityPostProcessor Nip40EventEntityPostProcessor() {
        return new Nip40EventEntityPostProcessor(eventEntityService);
    }
}
