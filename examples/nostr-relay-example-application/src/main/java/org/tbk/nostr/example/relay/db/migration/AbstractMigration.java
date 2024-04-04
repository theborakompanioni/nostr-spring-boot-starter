package org.tbk.nostr.example.relay.db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.tbk.nostr.example.relay.db.SupportedDatabaseType;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractMigration extends BaseJavaMigration {

    @Override
    public final void migrate(Context context) throws Exception {
        SupportedDatabaseType databaseType = SupportedDatabaseType.fromDatabaseType(context.getConfiguration().getDatabaseType());

        List<String> sqls = new ArrayList<>();
        if (databaseType == SupportedDatabaseType.POSTGRES) {
            sqls.addAll(postgres());
        } else if (databaseType == SupportedDatabaseType.SQLITE) {
            sqls.addAll(sqlite());
        } else {
            throw new IllegalStateException("Unsupported database");
        }

        for (String sql : sqls) {
            try (PreparedStatement statement = context.getConnection().prepareStatement(sql)) {
                statement.execute();
            }
        }
    }

    protected abstract Collection<String> postgres();

    protected abstract Collection<String> sqlite();
}
