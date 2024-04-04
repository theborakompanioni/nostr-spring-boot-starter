package org.tbk.nostr.example.relay.db;

import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.postgresql.PostgreSQLDatabaseType;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType;

public enum SupportedDatabaseType {
    POSTGRES,
    SQLITE;

    public static SupportedDatabaseType fromDatabaseType(DatabaseType databaseType) {
        if (new PostgreSQLDatabaseType().getName().equals(databaseType.getName())) {
            return POSTGRES;
        } else if (new SQLiteDatabaseType().getName().equals(databaseType.getName())) {
            return SQLITE;
        } else {
            throw new IllegalStateException("Unsupported database: %s".formatted(databaseType.getName()));
        }
    }
}
