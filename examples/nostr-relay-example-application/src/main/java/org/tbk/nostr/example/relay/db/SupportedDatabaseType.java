package org.tbk.nostr.example.relay.db;

import org.flywaydb.core.internal.database.DatabaseType;
import org.flywaydb.core.internal.database.sqlite.SQLiteDatabaseType;
import org.flywaydb.database.postgresql.PostgreSQLDatabaseType;

public enum SupportedDatabaseType {
    POSTGRESQL,
    SQLITE;

    public static SupportedDatabaseType fromDatabaseType(DatabaseType databaseType) {
        if (new PostgreSQLDatabaseType().getName().equals(databaseType.getName())) {
            return POSTGRESQL;
        } else if (new SQLiteDatabaseType().getName().equals(databaseType.getName())) {
            return SQLITE;
        } else {
            throw new IllegalStateException("Unsupported database: %s".formatted(databaseType.getName()));
        }
    }

    public static SupportedDatabaseType fromUrl(String url) {
        if (url.contains(":postgresql:")) {
            return POSTGRESQL;
        } else if (url.contains(":sqlite:")) {
            return SQLITE;
        } else {
            throw new IllegalStateException("Could not find supported database type.");
        }
    }
}
