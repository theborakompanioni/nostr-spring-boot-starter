package org.tbk.nostr.relay.example.nostr.support;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.handler.ReqRequestHandler;

@Slf4j
@RequiredArgsConstructor
public class MaxLimitPerFilterReqRequestHandlerDecorator implements ReqRequestHandler {

    @NonNull
    private final ReqRequestHandler delegate;

    private final int maxLimitPerFilter;

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        for (Filter filter : req.getFiltersList()) {
            if (filter.getLimit() > maxLimitPerFilter) {
                String message = "Maximum limit per filter in REQ message. Maximum is %d, got %d"
                        .formatted(maxLimitPerFilter, filter.getLimit());

                log.debug("Validation failed for REQ request: {}", message);

                session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                        .setClosed(ClosedResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setMessage("Error: %s".formatted(message))
                                .build())
                        .build())));
                return;
            }
        }

        delegate.handleReqMessage(session, req);
    }
}
