package org.tbk.nostr.example.relay.db.migration.sqlite;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class V1__sqlite_init extends BaseJavaMigration {

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
                create table if not exists event (
                    id text PRIMARY KEY,
                    pubkey text NOT NULL,
                    kind integer NOT NULL,
                    created_at integer NOT NULL,
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
