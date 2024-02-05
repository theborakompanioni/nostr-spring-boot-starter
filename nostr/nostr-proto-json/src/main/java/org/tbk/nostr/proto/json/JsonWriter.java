package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tbk.nostr.proto.json.Json.json;
import static org.tbk.nostr.proto.json.Json.jsonForSigning;

public final class JsonWriter {

    private JsonWriter() {
        throw new UnsupportedOperationException();
    }

    public static String toJson(Request val) {
        return switch (val.getKindCase()) {
            case Request.KindCase.EVENT -> toJson(val.getEvent());
            case Request.KindCase.REQ -> toJson(val.getReq());
            case Request.KindCase.CLOSE -> toJson(val.getClose());
            case Request.KindCase.COUNT -> toJson(val.getCount());
            case Request.KindCase.KIND_NOT_SET -> throw new IllegalArgumentException("Kind not set");
        };
    }

    public static String toJson(CloseRequest val) {
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

    public static String toJson(EventRequest val) {
        try {
            return json
                    .composeString()
                    .startArray()
                    .add("EVENT")
                    .addObject(asMap(val.getEvent()))
                    .end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String toJson(ReqRequest val) {
        return toJsonWithSubscriptionIdAndFilter("REQ", val.getId(), val.getFiltersList());
    }

    public static String toJson(CountRequest val) {
        return toJsonWithSubscriptionIdAndFilter("COUNT", val.getId(), val.getFiltersList());
    }

    public static String toJson(Metadata val) {
        try {
            return json
                    .composeString()
                    .startObject()
                    .put("name", val.getName())
                    .put("about", val.getAbout())
                    .put("picture", val.getPicture().toString())
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
                arrayComposer.addObject(asMap(it));
            }
            return arrayComposer.end().finish();
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
    public static String toJsonForSigning(Event.Builder e) {
        try {
            return jsonForSigning.composeString()
                    .startArray()
                    .add(0)
                    .add(HexFormat.of().formatHex(e.getPubkey().toByteArray()))
                    .add(e.getCreatedAt())
                    .add(e.getKind())
                    .addObject(listFromTags(e.getTagsList()))
                    .add(e.getContent())
                    .end()
                    .finish();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<List<String>> listFromTags(List<TagValue> tags) {
        return tags.stream()
                .map(it -> Stream.concat(Stream.of(it.getName()), it.getValuesList().stream()).toList())
                .toList();
    }

    /**
     * <code>
     * {
     * "id": <32-bytes lowercase hex-encoded sha256 of the serialized event data>,
     * "pubkey": <32-bytes lowercase hex-encoded public key of the event creator>,
     * "created_at": <unix timestamp in seconds>,
     * "kind": <integer between 0 and 65535>,
     * "tags": [
     * [<arbitrary string>...],
     * // ...
     * ],
     * "content": <arbitrary string>,
     * "sig": <64-bytes lowercase hex of the signature of the sha256 hash of the serialized event data, which is the same as the "id" field>
     * }
     * </code>
     */
    private static Map<String, Object> asMap(Event event) {
        return ImmutableMap.<String, Object>builder()
                .put("id", HexFormat.of().formatHex(event.getId().toByteArray()))
                .put("pubkey", HexFormat.of().formatHex(event.getPubkey().toByteArray()))
                .put("created_at", event.getCreatedAt())
                .put("kind", event.getKind())
                .put("tags", listFromTags(event.getTagsList()))
                .put("content", event.getContent())
                .put("sig", HexFormat.of().formatHex(event.getSig().toByteArray()))
                .build();
    }

    /**
     * <code>
     * {
     * "ids": <a list of event ids>,
     * "authors": <a list of lowercase pubkeys, the pubkey of an event must be one of these>,
     * "kinds": <a list of a kind numbers>,
     * "#<single-letter (a-zA-Z)>": <a list of tag values, for #e — a list of event ids, for #p — a list of pubkeys, etc.>,
     * "since": <an integer unix timestamp in seconds, events must be newer than this to pass>,
     * "until": <an integer unix timestamp in seconds, events must be older than this to pass>,
     * "limit": <maximum number of events relays SHOULD return in the initial query>
     * }
     * </code>
     */
    private static Map<String, Object> asMap(Filter filter) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        if (!filter.getIdsList().isEmpty()) {
            builder.put("ids", filter.getIdsList()
                    .stream().map(it -> HexFormat.of().formatHex(it.toByteArray()))
                    .toList());
        }
        if (!filter.getAuthorsList().isEmpty()) {
            builder.put("authors", filter.getAuthorsList().stream()
                    .map(it -> HexFormat.of().formatHex(it.toByteArray()))
                    .toList());
        }
        if (!filter.getKindsList().isEmpty()) {
            builder.put("kinds", filter.getKindsList());
        }

        Map<String, Set<String>> tags = filter.getTagsList().stream()
                .filter(it -> it.getName().length() == 1)
                .reduce(new HashMap<>(), (map, tag) -> {
                    map.merge(tag.getName(), new HashSet<>(tag.getValuesList()), (oldVals, newVals) -> Stream.concat(oldVals.stream(), newVals.stream()).collect(Collectors.toSet()));
                    return map;
                }, (a, b) -> b);

        tags.forEach((key, value) -> builder.put("#%s".formatted(key), value));

        if (filter.getSince() > 0L) {
            builder.put("since", filter.getSince());
        }
        if (filter.getUntil() > 0L) {
            builder.put("until", filter.getUntil());
        }
        if (filter.getLimit() > 0L) {
            builder.put("limit", filter.getLimit());
        }

        return builder.build();
    }

    /**
     * @param event an event
     * @return given event serialized as json
     */
    @VisibleForTesting
    static String toJson(Event event) {
        try {
            return json.asString(asMap(event));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    static String toJson(Filter filter) {
        try {
            return json.asString(asMap(filter));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
