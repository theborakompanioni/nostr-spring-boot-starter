package org.tbk.nostr.example.relay.domain.event;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.tbk.nostr.example.relay.NostrRelayExampleApplicationProperties;
import org.tbk.nostr.example.relay.db.SupportedDatabaseType;

@Slf4j
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class EventEntityConfiguration {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Bean
    EventEntityService eventEntityService(EventEntities events,
                                          NostrRelayExampleApplicationProperties properties,
                                          SupportedDatabaseType supportedDatabaseType) {
        return switch (supportedDatabaseType) {
            case POSTGRES -> new PostgresEventEntityService(events, properties);
            case SQLITE -> new SqliteEventEntityService(events, properties);
        };
    }

}
