package org.tbk.nostr.relay.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrRequestContext;

@Slf4j
@RequiredArgsConstructor
public class MaxLimitPerFilterInterceptor implements RequestHandlerInterceptor {

    private final int maxLimitPerFilter;

    @Override
    public boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() == Request.KindCase.REQ) {
            return handleReqMessage(context, request.getReq());
        }

        return true;
    }

    private boolean handleReqMessage(NostrRequestContext context, ReqRequest req) {
        for (Filter filter : req.getFiltersList()) {
            if (filter.getLimit() > maxLimitPerFilter) {
                String message = "Maximum limit per filter in REQ message. Maximum is %d, got %d"
                        .formatted(maxLimitPerFilter, filter.getLimit());

                log.debug("Validation failed for REQ request: {}", message);

                context.add(Response.newBuilder()
                        .setClosed(ClosedResponse.newBuilder()
                                .setSubscriptionId(req.getId())
                                .setMessage("Error: %s".formatted(message))
                                .build())
                        .build());
                return false;
            }
        }

        return true;
    }
}
