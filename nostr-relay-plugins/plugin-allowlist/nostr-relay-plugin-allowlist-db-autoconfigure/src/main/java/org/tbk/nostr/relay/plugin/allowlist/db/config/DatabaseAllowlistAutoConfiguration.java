package org.tbk.nostr.relay.plugin.allowlist.db.config;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.tbk.nostr.relay.plugin.allowlist.db.DatabaseAllowlist;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntries;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntryService;
import org.tbk.nostr.relay.plugin.allowlist.db.migration.V1__init;
import org.tbk.nostr.relay.plugin.allowlist.validation.AllowlistValidator;

import javax.sql.DataSource;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(AllowlistValidator.class)
@ConditionalOnProperty(value = "org.tbk.nostr.plugin.allowlist.enabled", matchIfMissing = true)
@ConditionalOnBean(DataSource.class)
@Import(StarterEntityRegistrar.class)
@AutoConfigureBefore(JpaRepositoriesAutoConfiguration.class)
public class DatabaseAllowlistAutoConfiguration {

    @Bean
    AllowlistEntryService allowlistEntryService2(AllowlistEntries entries) {
        return new AllowlistEntryService(entries);
    }

    @Bean
    @ConditionalOnMissingBean
    DatabaseAllowlist databaseAllowlist(AllowlistEntryService allowlistEntryService) {
        return new DatabaseAllowlist(allowlistEntryService);
    }

    @Bean
    InitializingBean initDatabaseAllowlistPlugin(DataSource dataSource) {
        return () -> {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .javaMigrations(new V1__init())
                    .target(MigrationVersion.LATEST)
                    .table("plugin_allowlist_flyway_schema_history")
                    .baselineVersion(MigrationVersion.fromVersion("0"))
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
        };
    }
}