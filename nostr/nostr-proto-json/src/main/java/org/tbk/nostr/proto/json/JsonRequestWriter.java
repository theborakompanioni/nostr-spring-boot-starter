package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.fasterxml.jackson.jr.ob.comp.ObjectComposer;
import com.google.common.annotations.VisibleForTesting;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.net.URI;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.tbk.nostr.proto.json.Json.json;
import static org.tbk.nostr.proto.json.Json.jsonForSigning;

final class JsonRequestWriter {

    private JsonRequestWriter() {
        throw new UnsupportedOperationException();
    }

    static String toJson(Request val) {
        return switch (val.getKindCase()) {
            case Request.KindCase.EVENT -> toJson(val.getEvent());
            case Request.KindCase.REQ -> toJson(val.getReq());
            case Request.KindCase.CLOSE -> toJson(val.getClose());
            case Request.KindCase.COUNT -> toJson(val.getCount());
            case Request.KindCase.KIND_NOT_SET -> throw new IllegalArgumentException("Kind not set");
        };
    }

    static String toJson(Metadata val) {
        try {
            ObjectComposer<JSONComposer<String>> builder = json
                    .composeString()
                    .startObject()
                    .put("name", val.getName())
                    .put("about", val.getAbout())
                    .put("picture", Optional.ofNullable(val.getPicture())
                            .map(URI::toString)
                            .orElse(null))
                    .put("display_name", val.getDisplayName())
                    .put("website", Optional.ofNullable(val.getWebsite())
                            .map(URI::toString)
                            .orElse(null))
                    .put("banner", Optional.ofNullable(val.getBanner())
                            .map(URI::toString)
                            .orElse(null));

            if (val.getBot() != null) {
                builder.put("bot", Boolean.TRUE.equals(val.getBot()));
            }
            if (val.getNip05() != null) {
                builder.put("nip05", val.getNip05());
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
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("EVENT")
                    .addObject(Json.asMap(val.getEvent()))
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String toJson(ReqRequest val) {
        return toJsonWithSubscriptionIdAndFilter("REQ", val.getId(), val.getFiltersList());
    }

    private static String toJson(CountRequest val) {
        return toJsonWithSubscriptionIdAndFilter("COUNT", val.getId(), val.getFiltersList());
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
