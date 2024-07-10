package org.tbk.nostr.example.relay.db;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.internal.database.DatabaseType;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

@Configuration(proxyBeanMethods = false)
class FlywayConfig {

    @Bean
    FlywayConfigurationCustomizer flywayConfigurationCustomizer(ApplicationContext applicationContext) {
        return configuration -> {
            DatabaseType databaseType = configuration.getDatabaseType();
            String pattern = "__%s_".formatted(databaseType.getName().toLowerCase(Locale.ROOT));
            JavaMigration[] javaMigrations = applicationContext.getBeansOfType(JavaMigration.class).values().stream()
                    .filter(it -> it.getClass().getSimpleName().contains(pattern))
                    .toArray(JavaMigration[]::new);

            configuration.javaMigrations(javaMigrations);
        };
    }

    @Bean
    SupportedDatabaseType supportedDatabaseType(Flyway flyway) {
        return SupportedDatabaseType.fromDatabaseType(flyway.getConfiguration().getDatabaseType());
    }
}
