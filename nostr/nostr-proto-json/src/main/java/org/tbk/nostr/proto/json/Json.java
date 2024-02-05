package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.TagValue;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    static Event fromMap(Map<String, Object> map, Event.Builder event) {
        List<Descriptors.FieldDescriptor> fields = Event.getDescriptor().getFields();
        for (Descriptors.FieldDescriptor field : fields) {
            Object value = map.get(field.getJsonName());
            if (value == null) {
                throw new IllegalArgumentException("Missing property '%s'".formatted(field.getJsonName()));
            }
        }

        @SuppressWarnings("unchecked")
        List<List<String>> tags = Optional.ofNullable((List<Object>) map.get("tags"))
                .orElseGet(Collections::emptyList).stream()
                .map(it -> (List<String>) it)
                .toList();

        return event
                .setId(ByteString.fromHex(String.valueOf(map.get("id"))))
                .setPubkey(ByteString.fromHex(String.valueOf(map.get("pubkey"))))
                .setCreatedAt(Long.parseLong(String.valueOf(map.get("created_at"))))
                .setKind(Integer.parseInt(String.valueOf(map.get("kind"))))
                .addAllTags(tagsFromList(tags))
                .setContent(String.valueOf(map.get("content")))
                .setSig(ByteString.fromHex(String.valueOf(map.get("sig"))))
                .build();
    }

    private static List<TagValue> tagsFromList(List<List<String>> tags) {
        return ImmutableList.<TagValue>builder()
                .addAll(tags.stream().filter(it -> !it.isEmpty())
                        .map(it -> TagValue.newBuilder()
                                .setName(it.getFirst())
                                .addAllValues(Iterables.skip(it, 1))
                                .build())
                        .toList())
                .build();
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
    static Map<String, Object> asMap(Event event) {
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

    static List<List<String>> listFromTags(List<TagValue> tags) {
        return tags.stream()
                .map(it -> Stream.concat(Stream.of(it.getName()), it.getValuesList().stream()).toList())
                .toList();
    }

    static Filter fromMap(Map<String, Object> map, Filter.Builder filter) {
        @SuppressWarnings("unchecked")
        List<ByteString> ids = Optional.ofNullable((List<Object>) map.get("ids"))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .distinct()
                .map(ByteString::fromHex)
                .toList();

        @SuppressWarnings("unchecked")
        List<ByteString> authors = Optional.ofNullable((List<Object>) map.get("authors"))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .distinct()
                .map(ByteString::fromHex)
                .toList();

        @SuppressWarnings("unchecked")
        List<Integer> kinds = Optional.ofNullable((List<Object>) map.get("kinds"))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .map(Integer::parseInt)
                .distinct()
                .collect(Collectors.toList());

        List<TagValue> singleLetterTags = map.entrySet().stream()
                .filter(entry -> entry.getKey().matches("#[a-zA-Z]"))
                .map(entry -> {
                    @SuppressWarnings("unchecked")
                    List<String> tagValues = Optional.ofNullable((List<Object>) entry.getValue())
                            .orElseGet(Collections::emptyList).stream()
                            .map(String::valueOf)
                            .distinct()
                            .collect(Collectors.toList());

                    return TagValue.newBuilder()
                            .setName(String.valueOf(entry.getKey().charAt(1)))
                            .addAllValues(tagValues)
                            .build();
                }).distinct()
                .toList();

        Optional<Long> since = Optional.ofNullable(map.get("since"))
                .map(String::valueOf)
                .map(Long::parseLong);

        Optional<Long> until = Optional.ofNullable(map.get("until"))
                .map(String::valueOf)
                .map(Long::parseLong);

        Optional<Integer> limit = Optional.ofNullable(map.get("limit"))
                .map(String::valueOf)
                .map(Integer::parseInt);

        Filter.Builder filterBuilder = filter
                .addAllIds(ids)
                .addAllAuthors(authors)
                .addAllKinds(kinds)
                .addAllTags(singleLetterTags);

        since.ifPresent(filterBuilder::setSince);
        until.ifPresent(filterBuilder::setUntil);
        limit.ifPresent(filterBuilder::setLimit);

        return filterBuilder.build();
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
    static Map<String, Object> asMap(Filter filter) {
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

        Map<String, Set<String>> singleLetterTags = filter.getTagsList().stream()
                .filter(it -> it.getName().length() == 1)
                .reduce(new HashMap<>(), (map, tag) -> {
                    map.merge(tag.getName(), new HashSet<>(tag.getValuesList()), (oldVals, newVals) -> Stream.concat(oldVals.stream(), newVals.stream()).collect(Collectors.toSet()));
                    return map;
                }, (a, b) -> b);

        singleLetterTags.forEach((key, value) -> builder.put("#%s".formatted(key), value));

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


    static String toJsonArray(JSON json, String... values) {
        try {
            ArrayComposer<JSONComposer<String>> arrayComposer = json
                    .composeString()
                    .startArray();

            for (String it : values) {
                arrayComposer.add(it);
            }

            return arrayComposer.end()
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
