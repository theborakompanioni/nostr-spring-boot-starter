package org.tbk.nostr.relay.plugin.allowlist.db.config;

import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.tbk.nostr.relay.plugin.allowlist.config.AllowlistPluginAutoConfiguration;
import org.tbk.nostr.relay.plugin.allowlist.config.AllowlistPluginProperties;
import org.tbk.nostr.relay.plugin.allowlist.db.DatabaseAllowlist;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntries;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntry;
import org.tbk.nostr.relay.plugin.allowlist.db.domain.AllowlistEntryService;
import org.tbk.nostr.relay.plugin.allowlist.db.migration.V1__init;
import org.tbk.nostr.relay.plugin.allowlist.validation.AllowlistValidator;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Slf4j
@AutoConfiguration
@ConditionalOnClass(AllowlistValidator.class)
@ConditionalOnProperty(value = "org.tbk.nostr.plugin.allowlist.enabled", matchIfMissing = true)
@ConditionalOnBean(DataSource.class)
@Import(StarterEntityRegistrar.class)
@AutoConfigureBefore({
        JpaRepositoriesAutoConfiguration.class,
        AllowlistPluginAutoConfiguration.class
})
@RequiredArgsConstructor
public class DatabaseAllowlistAutoConfiguration {
    @NonNull
    private final AllowlistPluginProperties properties;

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
    @ConditionalOnBean(DatabaseAllowlist.class)
    InitializingBean initDatabaseAllowlistPlugin(DataSource dataSource) {
        return () -> {
            Flyway flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .javaMigrations(new V1__init())
                    .table("plugin_allowlist_flyway_schema_history")
                    .baselineVersion(MigrationVersion.fromVersion("0"))
                    .baselineOnMigrate(true)
                    .load();

            flyway.migrate();
        };
    }

    @Bean
    @ConditionalOnProperty(value = "org.tbk.nostr.plugin.allowlist.db.init-from-properties", matchIfMissing = true)
    InitializingBean populateAllowlistTableFromProperties(AllowlistEntryService service,
                                                          @Qualifier("initDatabaseAllowlistPlugin") InitializingBean initDatabaseAllowlistPlugin) {
        return () -> {
            List<XonlyPublicKey> allowed = this.properties.getAllowed();
            allowed.forEach(publicKey -> {
                Optional<AllowlistEntry> allowlistEntry = service.findFirstByPubkey(publicKey);
                if (allowlistEntry.isEmpty()) {
                    service.create(publicKey);
                }
            });
        };
    }
}