package org.tbk.nostr.relay.example.nostr.interceptor;

import lombok.RequiredArgsConstructor;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

@RequiredArgsConstructor
public class MaxFilterCountReqRequestHandlerInterceptor implements RequestHandlerInterceptor {

    private final int maxFilterCount;

    @Override
    public boolean preHandle(NostrWebSocketSession session, Request request) {
        if (request.getKindCase() == Request.KindCase.REQ) {
            return handleReqMessage(session, request.getReq());
        }

        return true;
    }

    private boolean handleReqMessage(NostrWebSocketSession session, ReqRequest req) {
        if (req.getFiltersCount() <= maxFilterCount) {
            return true;
        }

        session.queueResponse(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId(req.getId())
                        .setMessage("Error: %s".formatted(
                                "Maximum filter in REQ message. Maximum is %d, got %d".formatted(maxFilterCount, req.getFiltersCount())
                        ))
                        .build())
                .build());

        return false;
    }
}
