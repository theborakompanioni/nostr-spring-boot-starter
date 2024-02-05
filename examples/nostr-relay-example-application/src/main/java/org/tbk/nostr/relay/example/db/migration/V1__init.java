package org.tbk.nostr.relay.example.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        String sql1 = """
                create table if not exists event (
                    id text PRIMARY KEY,
                    pubkey text NOT NULL,
                    kind integer NOT NULL,
                    created_at integer NOT NULL,
                    tags text,
                    content text NOT NULL,
                    sig blob NOT NULL,
                    -- other metadata
                    first_seen_at integer NOT NULL,
                    deleted integer,
                    expires_at integer
                ) STRICT, WITHOUT ROWID;
                """;

        for (String sql : List.of(sql1)) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }
}
