package org.tbk.nostr.proto.json;

import com.google.protobuf.ByteString;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

import static org.tbk.nostr.proto.json.Json.json;

final class JsonResponseReader {

    private JsonResponseReader() {
        throw new UnsupportedOperationException();
    }

    static Response fromJson(String val, Response.Builder response) {
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
                                        .setEvent(Json.fromMap(eventMap, Event.newBuilder()))
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
                    // ["COUNT", <subscription_id>, {"count": <integer>}]
                    case COUNT -> {
                        if (array.length < 3) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> countMap = (Map<String, Object>) array[2];
                        yield response.setCount(CountResponse.newBuilder()
                                        .setSubscriptionId(subscriptionId)
                                        .setResult(countFromMap(countMap, CountResult.newBuilder()))
                                        .build())
                                .build();
                    }
                    case AUTH -> {
                        if (array.length < 2) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String challenge = String.valueOf(array[1]);
                        yield response.setAuth(AuthResponse.newBuilder()
                                        .setChallenge(challenge)
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

    static Metadata fromJson(String val, Metadata.Builder metadata) {
        try {
            return metadataFromMap(json.mapFrom(val), metadata);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static CountResult countFromMap(Map<String, Object> map, CountResult.Builder count) {
        return count
                .setCount(Long.parseLong(String.valueOf(map.get("count"))))
                .setApproximate(Boolean.parseBoolean(String.valueOf(map.getOrDefault("approximate", false))))
                .build();
    }

    private static Metadata metadataFromMap(Map<String, Object> map, Metadata.Builder metadata) {
        Metadata.Builder builder = metadata
                .name(Optional.ofNullable(map.get("name"))
                        .map(String::valueOf)
                        .orElse(null))
                .about(Optional.ofNullable(map.get("about"))
                        .map(String::valueOf)
                        .orElse(null))
                .picture(Optional.ofNullable(map.get("picture"))
                        .map(String::valueOf)
                        .map(URI::create)
                        .orElse(null))
                .displayName(Optional.ofNullable(map.get("display_name"))
                        .map(String::valueOf)
                        .orElse(null))
                .website(Optional.ofNullable(map.get("website"))
                        .map(String::valueOf)
                        .map(URI::create)
                        .orElse(null))
                .banner(Optional.ofNullable(map.get("banner"))
                        .map(String::valueOf)
                        .map(URI::create)
                        .orElse(null));

        Optional.ofNullable(map.get("bot"))
                .map(String::valueOf)
                .map(Boolean::parseBoolean)
                .ifPresent(builder::bot);

        Optional.ofNullable(map.get("nip05"))
                .map(String::valueOf)
                .ifPresent(builder::nip05);

        Optional.ofNullable(map.get("lud16"))
                .map(String::valueOf)
                .ifPresent(builder::lud16);

        return builder.build();
    }
}
