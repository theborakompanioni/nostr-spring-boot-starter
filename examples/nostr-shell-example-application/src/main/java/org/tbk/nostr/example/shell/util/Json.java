package org.tbk.nostr.example.shell.util;

import com.fasterxml.jackson.jr.ob.JSON;

public class Json {

    public static final JSON json = JSON.std
            .with(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
            .with(JSON.Feature.READ_ONLY)
            .with(JSON.Feature.PRESERVE_FIELD_ORDERING)
            .with(JSON.Feature.USE_DEFERRED_MAPS)
            .without(JSON.Feature.FORCE_REFLECTION_ACCESS)
            .without(JSON.Feature.PRETTY_PRINT_OUTPUT);

    public static final JSON jsonPretty = json
            .with(JSON.Feature.PRETTY_PRINT_OUTPUT);
}
