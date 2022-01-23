package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonReader {
    private static final JSON json = JSON.std
            .with(JSON.Feature.FAIL_ON_DUPLICATE_MAP_KEYS)
            .with(JSON.Feature.PRETTY_PRINT_OUTPUT);

    private JsonReader() {
        throw new UnsupportedOperationException();
    }

    public static Response fromJsonResponse(String json) {
        return fromJson(json, Response.newBuilder());
    }

    public static Response fromJson(String val, Response.Builder response) {
        try {
            Object[] array = json.arrayFrom(val);
            if (array.length < 1) {
                throw new IllegalArgumentException("Could not parse passed arg");
            }
            if (array[0] instanceof String kindVal) {
                Response.KindCase kindCase = Response.KindCase.valueOf(kindVal);

                return switch (kindCase) {
                    // ["EVENT", <subscription_id>, <event JSON as defined above>]
                    case EVENT -> {
                        if (array.length < 3) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eventMap = (Map<String, Object>) array[2];
                        yield response.setEvent(EventResponse.newBuilder()
                                        .setSubscriptionId(subscriptionId)
                                        .setEvent(eventFromMap(eventMap, Event.newBuilder()))
                                        .build())
                                .build();
                    }
                    // ["OK", <event_id>, <true|false>, <message>]
                    case OK -> {
                        if (array.length < 4) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }

                        String eventId = String.valueOf(array[1]);
                        boolean success = Boolean.parseBoolean(String.valueOf(array[2]));
                        String message = String.valueOf(array[3]);

                        yield response.setOk(OkResponse.newBuilder()
                                        .setEventId(ByteString.fromHex(eventId))
                                        .setSuccess(success)
                                        .setMessage(message)
                                        .build())
                                .build();
                    }
                    // ["EOSE", <subscription_id>]
                    case EOSE -> {
                        if (array.length < 2) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);
                        yield response.setEose(EoseResponse.newBuilder()
                                        .setSubscriptionId(subscriptionId)
                                        .build())
                                .build();
                    }
                    // ["CLOSED", <subscription_id>, <message>]
                    case CLOSED -> {
                        if (array.length < 3) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);
                        String message = String.valueOf(array[2]);
                        yield response.setClosed(ClosedResponse.newBuilder()
                                        .setSubscriptionId(subscriptionId)
                                        .setMessage(message)
                                        .build())
                                .build();
                    }
                    // ["NOTICE", <message>]
                    case NOTICE -> {
                        if (array.length < 2) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String message = String.valueOf(array[1]);
                        yield response.setNotice(NoticeResponse.newBuilder()
                                        .setMessage(message)
                                        .build())
                                .build();
                    }
                    case KIND_NOT_SET -> throw new IllegalArgumentException("Kind not set");
                };
            } else {
                throw new IllegalArgumentException("Could not parse passed arg");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @VisibleForTesting
    public static Event fromJsonEvent(String json) {
        return fromJson(json, Event.newBuilder());
    }

    @VisibleForTesting
    public static Event fromJson(String val, Event.Builder event) {
        try {
            return eventFromMap(json.mapFrom(val), event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Event eventFromMap(Map<String, Object> eventMap, Event.Builder event) {
        @SuppressWarnings("unchecked")
        List<List<String>> tags = Optional.ofNullable((List<Object>) eventMap.get("tags"))
                .orElseGet(Collections::emptyList).stream()
                .map(it -> (List<String>) it).toList();
        return event
                .setId(ByteString.fromHex(String.valueOf(eventMap.get("id"))))
                .setPubkey(ByteString.fromHex(String.valueOf(eventMap.get("pubkey"))))
                .setCreatedAt(Long.parseLong(String.valueOf(eventMap.get("created_at"))))
                .setKind(Integer.parseInt(String.valueOf(eventMap.get("kind"))))
                .addAllTags(tagsFromList(tags))
                .setContent(String.valueOf(eventMap.get("content")))
                .setSig(ByteString.fromHex(String.valueOf(eventMap.get("sig"))))
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
}
