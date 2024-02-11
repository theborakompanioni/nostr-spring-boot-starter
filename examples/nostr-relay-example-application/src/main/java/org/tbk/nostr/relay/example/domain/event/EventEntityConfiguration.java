package org.tbk.nostr.relay.example.domain.event;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.relay.example.NostrRelayExampleApplicationProperties;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class EventEntityConfiguration {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Bean
    EventEntityService eventEntityService(EventEntities events) {
        return new EventEntityServiceImpl(events, properties);
    }
}
