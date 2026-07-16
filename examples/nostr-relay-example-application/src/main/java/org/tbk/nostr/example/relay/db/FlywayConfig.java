package org.tbk.nostr.example.relay.db;

import org.flywaydb.core.api.migration.JavaMigration;
import org.springframework.boot.flyway.autoconfigure.FlywayConfigurationCustomizer;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
class FlywayConfig {

    @Bean
    SupportedDatabaseType supportedDatabaseType(DataSourceProperties dataSourceProperties) {
        return SupportedDatabaseType.fromUrl(dataSourceProperties.determineUrl());
    }

    @Bean
    FlywayConfigurationCustomizer flywayConfigurationCustomizer(SupportedDatabaseType databaseType,
                                                                ApplicationContext applicationContext) {
        return configuration -> {
            String pattern = "__%s_".formatted(databaseType.name().toLowerCase(Locale.ROOT));
            JavaMigration[] javaMigrations = applicationContext.getBeansOfType(JavaMigration.class).values().stream()
                    .filter(it -> it.getClass().getSimpleName().contains(pattern))
                    .toArray(JavaMigration[]::new);

            configuration.javaMigrations(javaMigrations);
        };
    }
}
