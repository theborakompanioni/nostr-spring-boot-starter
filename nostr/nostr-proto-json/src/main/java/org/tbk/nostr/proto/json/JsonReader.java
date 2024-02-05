package org.tbk.nostr.proto.json;

import com.google.common.annotations.VisibleForTesting;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;

public final class JsonReader {

    private JsonReader() {
        throw new UnsupportedOperationException();
    }

    public static Request fromJson(String val, Request.Builder request) {
        return JsonRequestReader.fromJson(val, request);
    }

    public static Response fromJson(String val, Response.Builder response) {
        return JsonResponseReader.fromJson(val, response);
    }

    public static Metadata fromJson(String val, Metadata.Builder metadata) {
        return JsonResponseReader.fromJson(val, metadata);
    }

    @VisibleForTesting
    public static Event fromJson(String val, Event.Builder event) {
        return JsonResponseReader.fromJson(val, event);
    }
}
