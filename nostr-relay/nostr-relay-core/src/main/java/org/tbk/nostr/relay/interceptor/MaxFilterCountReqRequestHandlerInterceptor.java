package org.tbk.nostr.relay.interceptor;

import lombok.RequiredArgsConstructor;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.Request;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.relay.NostrRequestContext;

@RequiredArgsConstructor
public class MaxFilterCountReqRequestHandlerInterceptor implements RequestHandlerInterceptor {

    private final int maxFilterCount;

    @Override
    public boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.REQ) {
            return handleReqMessage(context, request.getReq());
        }

        return true;
    }

    private boolean handleReqMessage(NostrRequestContext context, ReqRequest req) {
        if (req.getFiltersCount() <= maxFilterCount) {
            return true;
        }

        context.add(Response.newBuilder()
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
