package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.Descriptors.FieldDescriptor;
import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

import static org.tbk.nostr.proto.json.Json.json;
import static org.tbk.nostr.proto.json.Json.jsonForSigning;

final class JsonRequestWriter {

    private JsonRequestWriter() {
        throw new UnsupportedOperationException();
    }

    static String toJson(Request val) {
        return switch (val.getKindCase()) {
            case EVENT -> toJson(val.getEvent());
            case REQ -> toJson(val.getReq());
            case CLOSE -> toJson(val.getClose());
            case COUNT -> toJson(val.getCount());
            case AUTH -> toJson(val.getAuth());
            case KIND_NOT_SET -> throw new IllegalArgumentException("Kind not set");
        };
    }

    static String toJson(Metadata val) {
        try {
            ObjectComposer<JSONComposer<String>> builder = json
                    .composeString()
                    .startObject();

            Map<FieldDescriptor, Object> allFields = val.getAllFields();
            for (Map.Entry<FieldDescriptor, Object> entry : allFields.entrySet()) {
                builder.putObject(entry.getKey().getJsonName(), entry.getValue());
            }
            return builder.end().finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * See: <a href="https://github.com/nostr-protocol/nips/blob/master/01.md">NIP-01</a>
     * <p>
     * <code>
     * [
     * 0,
     * <pubkey, as a lowercase hex string>,
     * <created_at, as a number>,
     * <kind, as a number>,
     * <tags, as an array of arrays of non-null strings>,
     * <content, as a string>
     * ]
     * </code>
     */
    static String toJsonForSigning(EventOrBuilder e) {
        try {
            return jsonForSigning.composeString()
                    .startArray()
                    .add(0)
                    .add(HexFormat.of().formatHex(e.getPubkey().toByteArray()))
                    .add(e.getCreatedAt())
                    .add(e.getKind())
                    .addObject(Json.listFromTags(e.getTagsList()))
                    .add(e.getContent())
                    .end()
                    .finish();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static String toJson(CloseRequest val) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("CLOSE")
                    .add(val.getId())
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(EventRequest val) {
        return toJsonWithEvent("EVENT", val.getEvent());
    }

    private static String toJson(AuthRequest val) {
        return toJsonWithEvent("AUTH", val.getEvent());
    }

    private static String toJson(ReqRequest val) {
        return toJsonWithSubscriptionIdAndFilter("REQ", val.getId(), val.getFiltersList());
    }

    private static String toJson(CountRequest val) {
        return toJsonWithSubscriptionIdAndFilter("COUNT", val.getId(), val.getFiltersList());
    }

    private static String toJsonWithEvent(String cmd, Event event) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add(cmd)
                    .addObject(Json.asMap(event))
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJsonWithSubscriptionIdAndFilter(String cmd, String subscriptionId, List<Filter> filters) {
        try {
            ArrayComposer<JSONComposer<String>> arrayComposer = json
                    .composeString()
                    .startArray()
                    .add(cmd)
                    .add(subscriptionId);

            for (Filter it : filters) {
                arrayComposer.addObject(Json.asMap(it));
            }
            return arrayComposer.end().finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param event an event
     * @return given event serialized as json
     */
    @VisibleForTesting
    static String toJson(Event event) {
        try {
            return json.asString(Json.asMap(event));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    static String toJson(Filter filter) {
        try {
            return json.asString(Json.asMap(filter));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
