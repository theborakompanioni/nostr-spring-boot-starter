package org.tbk.nostr.example.relay.db.migration.postgres;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.stream.Stream;

@Component
public class V2__postgresql_nip50_search extends BaseJavaMigration {

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
                create table if not exists event_nip50_meta_info (
                    event_id text PRIMARY KEY,
                    language_iso639_1 char(2) NOT NULL,
                    postgres_ts_config_cfgname text NOT NULL,
                    searchable_content text NOT NULL,
                    -- needed in order to stay compatible with sqlite FTS5 query syntax (a column named same as the table)
                    event_nip50_meta_info tsvector,
                    FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE
                );
                """;

        String sql2 = """
                CREATE INDEX en50mi_en50mi_idx ON event_nip50_meta_info USING GIN (event_nip50_meta_info) WHERE event_nip50_meta_info IS NOT NULL;
                """;

        String sql3 = """
                CREATE OR REPLACE FUNCTION update_event_nip50_meta_info_tsvector()
                      RETURNS TRIGGER AS $$
                      BEGIN
                          UPDATE event_nip50_meta_info SET
                            event_nip50_meta_info = to_tsvector(NEW.postgres_ts_config_cfgname::regconfig, coalesce(NEW.searchable_content, '')),
                            -- reset searchable_content to save disk space
                            searchable_content = ''
                            WHERE event_id = NEW.event_id;
                          RETURN NULL;
                      END;
                      $$ LANGUAGE plpgsql;
                """;

        String sql4 = """
                CREATE TRIGGER after_insert_event_nip50_meta_info_trigger
                    AFTER INSERT ON event_nip50_meta_info
                    FOR EACH ROW
                    EXECUTE FUNCTION update_event_nip50_meta_info_tsvector();
                """;

        return Stream.concat(
                Stream.of(sql1, sql2, sql3, sql4),
                Stream.empty()
        ).toList();
    }
}
