package org.tbk.nostr.proto.json;

import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.util.HexFormat;

import static org.tbk.nostr.proto.json.Json.json;

final class JsonResponseWriter {

    private JsonResponseWriter() {
        throw new UnsupportedOperationException();
    }

    static String toJson(Response val) {
        return switch (val.getKindCase()) {
            case EVENT -> toJson(val.getEvent());
            case OK -> toJson(val.getOk());
            case EOSE -> toJson(val.getEose());
            case CLOSED -> toJson(val.getClosed());
            case NOTICE -> toJson(val.getNotice());
            case COUNT -> toJson(val.getCount());
            case Response.KindCase.KIND_NOT_SET -> throw new IllegalArgumentException("Kind not set");
        };
    }

    private static String toJson(EventResponse val) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("EVENT")
                    .add(val.getSubscriptionId())
                    .addObject(Json.asMap(val.getEvent()))
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(OkResponse val) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("OK")
                    .add(HexFormat.of().formatHex(val.getEventId().toByteArray()))
                    .add(val.getSuccess())
                    .add(val.getMessage())
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(EoseResponse val) {
        return Json.toJsonArray(json, "EOSE", val.getSubscriptionId());
    }

    private static String toJson(ClosedResponse val) {
        return Json.toJsonArray(json, "CLOSED", val.getSubscriptionId(), val.getMessage());
    }

    private static String toJson(NoticeResponse val) {
        return Json.toJsonArray(json, "NOTICE", val.getMessage());
    }

    private static String toJson(CountResponse val) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("COUNT")
                    .add(val.getSubscriptionId())
                    .startObject()
                    .put("count", val.getResult().getCount())
                    .put("approximate", val.getResult().getApproximate())
                    .end()
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
