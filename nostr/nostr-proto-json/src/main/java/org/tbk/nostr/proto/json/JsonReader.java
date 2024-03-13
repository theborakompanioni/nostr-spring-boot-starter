package org.tbk.nostr.proto.json;

import com.google.common.annotations.VisibleForTesting;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;

import java.io.IOException;

import static org.tbk.nostr.proto.json.Json.json;

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

    public static Event fromJson(String val, Event.Builder event) {
        try {
            return Json.fromMap(json.mapFrom(val), event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Event.Builder fromJsonPartial(String val, Event.Builder event) {
        try {
            return Json.fromMapPartial(json.mapFrom(val), event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
