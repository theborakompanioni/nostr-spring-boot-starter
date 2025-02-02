package org.tbk.nostr.relay.plugin.allowlist.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;


@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        for (String sql : sql()) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    private List<String> sql() {
        String sql1 = """
                create table if not exists plugin_allowlist_entry (
                    id text PRIMARY KEY,
                    pubkey text NOT NULL,
                    created_at bigint NOT NULL,
                    expires_at bigint
                );
                """;

        String sql2 = """
                create index idx_plugin_allowlist_pubkey_expires_at ON plugin_allowlist_entry (pubkey, expires_at)
                """;

        return List.of(sql1, sql2);
    }
}
