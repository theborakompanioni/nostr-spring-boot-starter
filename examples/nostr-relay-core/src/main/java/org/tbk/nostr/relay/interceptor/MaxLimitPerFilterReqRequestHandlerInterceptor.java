package org.tbk.nostr.relay.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.relay.NostrWebSocketSession;

@Slf4j
@RequiredArgsConstructor
public class MaxLimitPerFilterReqRequestHandlerInterceptor implements RequestHandlerInterceptor {

    private final int maxLimitPerFilter;

    @Override
    public boolean preHandle(NostrWebSocketSession session, Request request) throws Exception {
        if (request.getKindCase() == Request.KindCase.REQ) {
            return handleReqMessage(session, request.getReq());
        }

        return true;
    }

    private boolean handleReqMessage(NostrWebSocketSession session, ReqRequest req) throws Exception {
        for (Filter filter : req.getFiltersList()) {
            if (filter.getLimit() > maxLimitPerFilter) {
                String message = "Maximum limit per filter in REQ message. Maximum is %d, got %d"
                        .formatted(maxLimitPerFilter, filter.getLimit());

                log.debug("Validation failed for REQ request: {}", message);

                session.sendResponseImmediately(Response.newBuilder()
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
