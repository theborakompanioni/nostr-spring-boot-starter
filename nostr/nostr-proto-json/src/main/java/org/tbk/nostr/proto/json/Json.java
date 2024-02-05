package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;

final class Json {

    static final JSON json = JSON.std
            .with(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
            .with(JSON.Feature.READ_ONLY)
            .with(JSON.Feature.PRESERVE_FIELD_ORDERING)
            .with(JSON.Feature.USE_DEFERRED_MAPS)
            .without(JSON.Feature.FORCE_REFLECTION_ACCESS)
            .without(JSON.Feature.PRETTY_PRINT_OUTPUT);

    static final JSON jsonPretty = json
            .with(JSON.Feature.PRETTY_PRINT_OUTPUT);

    static final JSON jsonForSigning = json
            .without(JSON.Feature.PRETTY_PRINT_OUTPUT);
}
