package org.tbk.nostr.example.relay.db.migration;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

@Component
public class V2__nip50_search extends AbstractMigration {

    @Override
    protected Collection<String> postgres() {
        return new Postgres().sql();
    }

    @Override
    protected Collection<String> sqlite() {
        return new Sqlite().sql();
    }

    private static class Postgres {
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

    private static class Sqlite {

        private List<String> sql() {
            /*String sql1 = """
                    create table if not exists event_nip50_meta_info (
                        event_id text PRIMARY KEY,
                        language_iso639_1 text NOT NULL,
                        searchable_content text NOT NULL,
                        FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE
                    ) STRICT, WITHOUT ROWID;
                    """;*/
            String sql1 = """
                    create virtual table if not exists event_nip50_meta_info using fts5(
                        event_id UNINDEXED,
                        postgres_ts_config_cfgname UNINDEXED,
                        language_iso639_1 UNINDEXED,
                        searchable_content
                    );
                    """;

            /*String sql2 = """
                    create virtual table if not exists event_nip50_fts using fts5(
                        searchable_content,
                        content = 'event_nip50_meta_info',
                        content_rowid = 'event_id',
                        tokenize = 'porter'
                    );
                    """;

            String sql3 = """
                    create virtual table if not exists event_nip50_fts2 using fts5(
                        event_id UNINDEXED,
                        searchable_content,
                        content = 'event_nip50_meta_info'
                    );
                    """;*/

            return Stream.concat(
                    Stream.of(sql1),
                    //Stream.of(sql2, sql3)
                    Stream.empty()
            ).toList();
        }
    }
}
