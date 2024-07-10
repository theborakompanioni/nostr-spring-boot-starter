package org.tbk.nostr.example.relay.db.migration.sqlite;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Stream;

@Component
public class V2__sqlite_nip50_search extends BaseJavaMigration {

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
                create virtual table if not exists event_nip50_meta_info using fts5 (
                    event_id UNINDEXED,
                    postgres_ts_config_cfgname UNINDEXED,
                    language_iso639_1 UNINDEXED,
                    searchable_content,
                    tokenize = 'porter'
                );
                """;

        // TODO: This can surely be done in a better way, but the following is unfortunately not working as expected :/
        //  (Keeping in mind the querying will be done with Specification and CriteriaBuilder)
        /*
        String sql1 = """
                create table if not exists event_nip50_meta_info (
                    event_id text PRIMARY KEY,
                    language_iso639_1 text NOT NULL,
                    postgres_ts_config_cfgname text NOT NULL,
                    searchable_content text NOT NULL,
                    -- unused
                    event_nip50_meta_info text,
                    event_nip50_meta_info_sqlite text,
                    FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE
                ) STRICT;
                """;

        String sql2 = """
                create virtual table if not exists event_nip50_fts using fts5 (
                    language_iso639_1,
                    searchable_content,
                    content = 'event_nip50_meta_info',
                    content_rowid = 'event_id',
                    tokenize = 'porter'
                );
                """;

        String sql3 = """
                CREATE TRIGGER after_insert_event_nip50_meta_info_trigger AFTER INSERT ON event_nip50_meta_info BEGIN
                    INSERT INTO event_nip50_fts(rowid, language_iso639_1, searchable_content) VALUES (new.event_id, new.language_iso639_1, new.searchable_content);
                    -- reset searchable_content to save disk space
                    -- UPDATE event_nip50_meta_info SET searchable_content = '' WHERE event_id = NEW.event_id;
                    END;
                """;
         */

        return Stream.concat(
                Stream.of(sql1),
                Stream.empty()
        ).toList();
    }
}
