package org.tbk.nostr.proto.json;

import com.google.common.annotations.VisibleForTesting;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;

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

}
