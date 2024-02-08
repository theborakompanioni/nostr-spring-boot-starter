package org.tbk.nostr.relay.example.db;

import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
