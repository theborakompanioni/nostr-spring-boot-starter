package org.tbk.nostr.proto.json;

import org.tbk.nostr.proto.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.tbk.nostr.proto.json.Json.json;

final class JsonRequestReader {

    private JsonRequestReader() {
        throw new UnsupportedOperationException();
    }

    static Request fromJson(String val, Request.Builder request) {
        try {
            Object[] array = json.arrayFrom(val);
            if (array.length < 1) {
                throw new IllegalArgumentException("Could not parse passed arg");
            }
            if (array[0] instanceof String kindVal) {
                Request.KindCase kindCase = Request.KindCase.valueOf(kindVal);

                return switch (kindCase) {
                    // ["EVENT", <event JSON as defined above>]
                    case EVENT -> {
                        if (array.length < 2) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eventMap = (Map<String, Object>) array[1];
                        yield request.setEvent(EventRequest.newBuilder()
                                        .setEvent(Json.fromMap(eventMap, Event.newBuilder()))
                                        .build())
                                .build();
                    }
                    // ["REQ", <subscription_id>, <filters1>, <filters2>, ...]
                    case REQ -> {
                        if (array.length < 3) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }

                        String subscriptionId = String.valueOf(array[1]);
                        List<Map<String, Object>> filterMaps = Stream.of(array).skip(2)
                                .map(it -> {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> filterMap = (Map<String, Object>) it;
                                    return filterMap;
                                })
                                .distinct()
                                .toList();

                        yield request.setReq(ReqRequest.newBuilder()
                                        .setId(subscriptionId)
                                        .addAllFilters(filterMaps.stream()
                                                .map(it -> Json.fromMap(it, Filter.newBuilder()))
                                                .distinct()
                                                .toList())
                                        .build())
                                .build();
                    }
                    // ["CLOSE", <subscription_id>]
                    case CLOSE -> {
                        if (array.length < 2) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);

                        yield request.setClose(CloseRequest.newBuilder()
                                        .setId(subscriptionId)
                                        .build())
                                .build();
                    }
                    // ["COUNT", <subscription_id>, {"kinds": [3], "#p": [<pubkey>]}]
                    case COUNT -> {
                        if (array.length < 3) {
                            throw new IllegalArgumentException("Could not parse passed arg");
                        }
                        String subscriptionId = String.valueOf(array[1]);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> filterMap = (Map<String, Object>) array[2];

                        yield request.setCount(CountRequest.newBuilder()
                                        .setId(subscriptionId)
                                        .addFilters(Json.fromMap(filterMap, Filter.newBuilder()))
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
}
