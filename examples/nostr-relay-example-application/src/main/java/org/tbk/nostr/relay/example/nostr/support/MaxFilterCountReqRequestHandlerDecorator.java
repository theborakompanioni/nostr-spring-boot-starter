package org.tbk.nostr.relay.example.nostr.support;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.relay.example.nostr.handler.ReqRequestHandler;

@RequiredArgsConstructor
public class MaxFilterCountReqRequestHandlerDecorator implements ReqRequestHandler {

    @NonNull
    private final ReqRequestHandler delegate;

    private final int maxFilterCount;

    @Override
    public void handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        if (req.getFiltersCount() <= maxFilterCount) {
            delegate.handleReqMessage(session, req);
        } else {
            session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                    .setClosed(ClosedResponse.newBuilder()
                            .setSubscriptionId(req.getId())
                            .setMessage("Error: %s".formatted(
                                    "Maximum filter in REQ message. Maximum is %d, got %d".formatted(maxFilterCount, req.getFiltersCount())
                            ))
                            .build())
                    .build())));
        }
    }
}
