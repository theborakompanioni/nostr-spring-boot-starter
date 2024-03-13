package org.tbk.nostr.proto.json;

import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;

import java.io.IOException;

import static org.tbk.nostr.proto.json.Json.json;

public final class JsonWriter {

    private JsonWriter() {
        throw new UnsupportedOperationException();
    }

    public static String toJson(Response val) {
        return JsonResponseWriter.toJson(val);
    }

    public static String toJson(Request val) {
        return JsonRequestWriter.toJson(val);
    }

    public static String toJson(Metadata val) {
        return JsonRequestWriter.toJson(val);
    }

    public static String toJsonForSigning(EventOrBuilder event) {
        return JsonRequestWriter.toJsonForSigning(event);
    }

    public static String toJson(Event val) {
        try {
            return json
                    .composeString()
                    .addObject(Json.asMap(val))
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
