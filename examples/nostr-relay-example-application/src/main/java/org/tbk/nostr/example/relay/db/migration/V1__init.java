package org.tbk.nostr.example.relay.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class V1__init extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        DatabaseType databaseType = context.getConfiguration().getDatabaseType();

        List<String> sqls = new ArrayList<>();
        if (databaseType.getName().equals(new PostgreSQLDatabaseType().getName())) {
            sqls.addAll(new Postgres().sql());
        } else if (databaseType.getName().equals(new SQLiteDatabaseType().getName())) {
            sqls.addAll(new Sqlite().sql());
        } else {
            throw new IllegalStateException("Unsupported database");
        }

        for (String sql : sqls) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    private static class Postgres {
        private List<String> sql() {
            String sql1 = """
                    create table if not exists event (
                        id text PRIMARY KEY,
                        pubkey text NOT NULL,
                        kind integer NOT NULL,
                        created_at bigint NOT NULL,
                        tags text,
                        content text NOT NULL,
                        sig bytea NOT NULL,
                        -- other metadata
                        first_seen_at bigint NOT NULL,
                        deleted_at bigint,
                        expires_at bigint
                    );
                    """;

            String sql2 = """
                    create index idx_event_pubkey_kind_created_at ON event (pubkey, kind, created_at)
                    """;

            String sql3 = """
                    create table if not exists event_tag (
                        event_id text NOT NULL,
                        position integer NOT NULL,
                        name text NOT NULL,
                        value0 text NULL,
                        value1 text NULL,
                        value2 text NULL,
                        other_values text NULL,
                        FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE,
                        PRIMARY KEY (event_id, position)
                    );
                    """;

            String abc = "abcdefghijklmnopqrstuvwxyz";
            List<String> singleLetterTagsIndexSqlList = Stream.concat(
                            Arrays.stream(abc.toLowerCase(Locale.US).split("")),
                            Arrays.stream(abc.toUpperCase(Locale.US).split(""))
                    ).map(it -> """
                            create index idx_event_tag_%s_%s ON event_tag (name, value0) WHERE name = '%s';
                            """.formatted(Character.isUpperCase(it.charAt(0)) ? "upper" : "lower", it, it))
                    .toList();

            return Stream.concat(
                    Stream.of(sql1, sql2, sql3),
                    singleLetterTagsIndexSqlList.stream()
            ).toList();
        }
    }

    private static class Sqlite {

        private List<String> sql() {
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
                        deleted_at integer,
                        expires_at integer
                    ) STRICT, WITHOUT ROWID;
                    """;

            String sql2 = """
                    create index idx_event_pubkey_kind_created_at ON event(pubkey, kind, created_at)
                    """;

            String sql3 = """
                    create table if not exists event_tag (
                        event_id text NOT NULL,
                        position integer NOT NULL,
                        name text NOT NULL,
                        value0 text NULL,
                        value1 text NULL,
                        value2 text NULL,
                        other_values text NULL,
                        FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE,
                        PRIMARY KEY (event_id, position)
                    ) STRICT, WITHOUT ROWID;
                    """;

            String abc = "abcdefghijklmnopqrstuvwxyz";
            List<String> singleLetterTagsIndexSqlList = Stream.concat(
                            Arrays.stream(abc.toLowerCase(Locale.US).split("")),
                            Arrays.stream(abc.toUpperCase(Locale.US).split(""))
                    ).map(it -> """
                            create index idx_event_tag_%s_%s ON event_tag(name = '%s', value0);
                            """.formatted(Character.isUpperCase(it.charAt(0)) ? "upper" : "lower", it, it))
                    .toList();

            return Stream.concat(
                    Stream.of(sql1, sql2, sql3),
                    singleLetterTagsIndexSqlList.stream()
            ).toList();
        }
    }
}
