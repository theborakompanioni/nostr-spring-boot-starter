package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.fasterxml.jackson.jr.ob.JSONComposer;
import com.fasterxml.jackson.jr.ob.comp.ArrayComposer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.TagFilter;
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

    private static final Descriptors.Descriptor eventDescriptor = Event.getDescriptor();
    private static final FieldDescriptor eventIdField = eventDescriptor.findFieldByNumber(Event.ID_FIELD_NUMBER);
    private static final FieldDescriptor eventPubkeyField = eventDescriptor.findFieldByNumber(Event.PUBKEY_FIELD_NUMBER);
    private static final FieldDescriptor eventCreatedAtField = eventDescriptor.findFieldByNumber(Event.CREATED_AT_FIELD_NUMBER);
    private static final FieldDescriptor eventKindField = eventDescriptor.findFieldByNumber(Event.KIND_FIELD_NUMBER);
    private static final FieldDescriptor eventTagsField = eventDescriptor.findFieldByNumber(Event.TAGS_FIELD_NUMBER);
    private static final FieldDescriptor eventContentField = eventDescriptor.findFieldByNumber(Event.CONTENT_FIELD_NUMBER);
    private static final FieldDescriptor eventSigField = eventDescriptor.findFieldByNumber(Event.SIG_FIELD_NUMBER);

    private static final Descriptors.Descriptor filterDescriptor = Filter.getDescriptor();
    private static final FieldDescriptor filterIdsField = filterDescriptor.findFieldByNumber(Filter.IDS_FIELD_NUMBER);
    private static final FieldDescriptor filterAuthorsField = filterDescriptor.findFieldByNumber(Filter.AUTHORS_FIELD_NUMBER);
    private static final FieldDescriptor filterKindsField = filterDescriptor.findFieldByNumber(Filter.KINDS_FIELD_NUMBER);
    private static final FieldDescriptor filterSinceField = filterDescriptor.findFieldByNumber(Filter.SINCE_FIELD_NUMBER);
    private static final FieldDescriptor filterUntilField = filterDescriptor.findFieldByNumber(Filter.UNTIL_FIELD_NUMBER);
    private static final FieldDescriptor filterLimitField = filterDescriptor.findFieldByNumber(Filter.LIMIT_FIELD_NUMBER);
    private static final FieldDescriptor filterSearchField = filterDescriptor.findFieldByNumber(Filter.SEARCH_FIELD_NUMBER);

    static Event fromMap(Map<String, Object> map, Event.Builder event) {
        List<FieldDescriptor> fields = Event.getDescriptor().getFields();
        for (FieldDescriptor field : fields) {
            Object value = map.get(field.getJsonName());
            if (value == null) {
                throw new IllegalArgumentException("Missing property '%s'".formatted(field.getJsonName()));
            }
        }

        @SuppressWarnings("unchecked")
        List<List<String>> tags = Optional.ofNullable((List<Object>) map.get(eventTagsField.getJsonName()))
                .orElseGet(Collections::emptyList).stream()
                .map(it -> (List<String>) it)
                .toList();

        return event
                .setId(ByteString.fromHex(String.valueOf(map.get(eventIdField.getJsonName()))))
                .setPubkey(ByteString.fromHex(String.valueOf(map.get(eventPubkeyField.getJsonName()))))
                .setCreatedAt(Long.parseLong(String.valueOf(map.get(eventCreatedAtField.getJsonName()))))
                .setKind(Integer.parseInt(String.valueOf(map.get(eventKindField.getJsonName())), 10))
                .addAllTags(tagsFromList(tags))
                .setContent(String.valueOf(map.get(eventContentField.getJsonName())))
                .setSig(ByteString.fromHex(String.valueOf(map.get(eventSigField.getJsonName()))))
                .build();
    }

    static Event.Builder fromMapPartial(Map<String, Object> map, Event.Builder event) {
        List<FieldDescriptor> fields = Event.getDescriptor().getFields();
        for (FieldDescriptor field : fields) {
            Object value = map.get(field.getJsonName());
            if (value != null) {
                switch (field.getJsonName()) {
                    case "id":
                        event.setId(ByteString.fromHex(String.valueOf(value)));
                        break;
                    case "pubkey":
                        event.setPubkey(ByteString.fromHex(String.valueOf(value)));
                        break;
                    case "kind":
                        event.setKind(Integer.parseInt(String.valueOf(value), 10));
                        break;
                    case "created_at":
                        event.setCreatedAt(Long.parseLong(String.valueOf(value)));
                        break;
                    case "content":
                        event.setContent(String.valueOf(value));
                        break;
                    case "sig":
                        event.setSig(ByteString.fromHex(String.valueOf(value)));
                        break;
                }
            }
        }

        @SuppressWarnings("unchecked")
        List<List<String>> tags = Optional.ofNullable((List<Object>) map.get(eventTagsField.getJsonName()))
                .orElseGet(Collections::emptyList).stream()
                .map(it -> (List<String>) it)
                .toList();
        List<TagValue> tagsFromProto = List.copyOf(event.getTagsList());

        return event
                .clearTags()
                .addAllTags(!tags.isEmpty() ? tagsFromList(tags) : tagsFromProto);
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
    static Map<String, Object> asMap(Event val) {
        return ImmutableMap.<String, Object>builder()
                .put(eventIdField.getJsonName(), HexFormat.of().formatHex(val.getId().toByteArray()))
                .put(eventPubkeyField.getJsonName(), HexFormat.of().formatHex(val.getPubkey().toByteArray()))
                .put(eventCreatedAtField.getJsonName(), val.getCreatedAt())
                .put(eventKindField.getJsonName(), val.getKind())
                .put(eventTagsField.getJsonName(), listFromTags(val.getTagsList()))
                .put(eventContentField.getJsonName(), val.getContent())
                .put(eventSigField.getJsonName(), HexFormat.of().formatHex(val.getSig().toByteArray()))
                .build();
    }

    static List<List<String>> listFromTags(List<TagValue> tags) {
        return tags.stream()
                .map(it -> Stream.concat(Stream.of(it.getName()), it.getValuesList().stream()).toList())
                .toList();
    }

    static Filter fromMap(Map<String, Object> map, Filter.Builder builder) {
        @SuppressWarnings("unchecked")
        List<ByteString> ids = Optional.ofNullable((List<Object>) map.get(filterIdsField.getJsonName()))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .distinct()
                .map(ByteString::fromHex)
                .toList();

        @SuppressWarnings("unchecked")
        List<ByteString> authors = Optional.ofNullable((List<Object>) map.get(filterAuthorsField.getJsonName()))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .distinct()
                .map(ByteString::fromHex)
                .toList();

        @SuppressWarnings("unchecked")
        List<Integer> kinds = Optional.ofNullable((List<Object>) map.get(filterKindsField.getJsonName()))
                .orElseGet(Collections::emptyList).stream()
                .map(String::valueOf)
                .map(Integer::parseInt)
                .distinct()
                .collect(Collectors.toList());

        List<TagFilter> singleLetterTags = map.entrySet().stream()
                .filter(entry -> entry.getKey().matches("#[a-zA-Z]"))
                .map(entry -> {
                    @SuppressWarnings("unchecked")
                    List<String> tagValues = Optional.ofNullable((List<Object>) entry.getValue())
                            .orElseGet(Collections::emptyList).stream()
                            .map(String::valueOf)
                            .distinct()
                            .collect(Collectors.toList());

                    return TagFilter.newBuilder()
                            .setName(String.valueOf(entry.getKey().charAt(1)))
                            .addAllValues(tagValues)
                            .build();
                }).distinct()
                .toList();

        Optional<Long> since = Optional.ofNullable(map.get(filterSinceField.getJsonName()))
                .map(String::valueOf)
                .map(Long::parseLong);

        Optional<Long> until = Optional.ofNullable(map.get(filterUntilField.getJsonName()))
                .map(String::valueOf)
                .map(Long::parseLong);

        Optional<Integer> limit = Optional.ofNullable(map.get(filterLimitField.getJsonName()))
                .map(String::valueOf)
                .map(Integer::parseInt);

        Optional<String> search = Optional.ofNullable(map.get(filterSearchField.getJsonName()))
                .map(String::valueOf);

        Filter.Builder filterBuilder = builder
                .addAllIds(ids)
                .addAllAuthors(authors)
                .addAllKinds(kinds)
                .addAllTags(singleLetterTags);

        since.ifPresent(filterBuilder::setSince);
        until.ifPresent(filterBuilder::setUntil);
        limit.ifPresent(filterBuilder::setLimit);
        search.ifPresent(filterBuilder::setSearch);

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
            builder.put(filterIdsField.getJsonName(), filter.getIdsList()
                    .stream().map(it -> HexFormat.of().formatHex(it.toByteArray()))
                    .toList());
        }
        if (!filter.getAuthorsList().isEmpty()) {
            builder.put(filterAuthorsField.getJsonName(), filter.getAuthorsList().stream()
                    .map(it -> HexFormat.of().formatHex(it.toByteArray()))
                    .toList());
        }
        if (!filter.getKindsList().isEmpty()) {
            builder.put(filterKindsField.getJsonName(), filter.getKindsList());
        }

        Map<String, Set<String>> singleLetterTags = filter.getTagsList().stream()
                .filter(it -> it.getName().length() == 1)
                .reduce(new HashMap<>(), (map, tag) -> {
                    map.merge(tag.getName(), new HashSet<>(tag.getValuesList()), (oldVals, newVals) -> Stream.concat(oldVals.stream(), newVals.stream()).collect(Collectors.toSet()));
                    return map;
                }, (a, b) -> b);

        singleLetterTags.forEach((key, value) -> builder.put("#%s".formatted(key), value));

        if (filter.getSince() > 0L) {
            builder.put(filterSinceField.getJsonName(), filter.getSince());
        }
        if (filter.getUntil() > 0L) {
            builder.put(filterUntilField.getJsonName(), filter.getUntil());
        }
        if (filter.getLimit() > 0L) {
            builder.put(filterLimitField.getJsonName(), filter.getLimit());
        }
        if (!filter.getSearch().isEmpty()) {
            builder.put(filterSearchField.getJsonName(), filter.getSearch());
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

            return arrayComposer.end().finish();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
