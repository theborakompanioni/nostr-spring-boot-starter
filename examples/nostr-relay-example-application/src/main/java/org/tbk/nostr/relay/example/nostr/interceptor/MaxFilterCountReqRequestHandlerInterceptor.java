package org.tbk.nostr.relay.example.nostr.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.proto.json.JsonWriter;

@RequiredArgsConstructor
public class MaxFilterCountReqRequestHandlerInterceptor implements NostrRequestHandlerInterceptor {

    private final int maxFilterCount;

    @Override
    public boolean preHandle(WebSocketSession session, Request request) throws Exception {
        if (request.getKindCase() == Request.KindCase.REQ) {
            return handleReqMessage(session, request.getReq());
        }

        return true;
    }

    private boolean handleReqMessage(WebSocketSession session, ReqRequest req) throws Exception {
        if (req.getFiltersCount() <= maxFilterCount) {
            return true;
        }

        session.sendMessage(new TextMessage(JsonWriter.toJson(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(req.getId())
                        .setMessage("Error: %s".formatted(
                                "Maximum filter in REQ message. Maximum is %d, got %d".formatted(maxFilterCount, req.getFiltersCount())
                        ))
                        .build())
                .build())));

        return false;
    }
}
