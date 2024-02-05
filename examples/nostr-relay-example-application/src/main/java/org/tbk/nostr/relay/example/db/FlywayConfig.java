package org.tbk.nostr.relay.example.db;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration(proxyBeanMethods = false)
class FlywayConfig {

    @Bean
    FlywayConfigurationCustomizer flywayConfigurationCustomizer(ApplicationContext applicationContext) {
        return configuration -> {
            JavaMigration[] javaMigrations = applicationContext.getBeansOfType(JavaMigration.class)
                    .values().toArray(JavaMigration[]::new);

            configuration.javaMigrations(javaMigrations);
        };
    }
}
