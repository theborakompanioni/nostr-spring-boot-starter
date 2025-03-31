package org.tbk.nostr.example.relay.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
class FlywayConfig {

    @Bean
    SupportedDatabaseType supportedDatabaseType(DataSourceProperties dataSourceProperties) {
        return SupportedDatabaseType.fromUrl(dataSourceProperties.getUrl());
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
