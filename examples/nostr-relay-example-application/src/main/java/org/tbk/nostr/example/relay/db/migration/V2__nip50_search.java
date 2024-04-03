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
                        searchable_content text NOT NULL,
                        -- needed in order to stay compatible with sqlite FTS5 query syntax (a column named same as the table)
                        event_nip50_meta_info text,
                        FOREIGN KEY(event_id) REFERENCES event(id) ON DELETE CASCADE ON UPDATE CASCADE
                    );
                    """;

            return Stream.concat(
                    Stream.of(sql1),
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
