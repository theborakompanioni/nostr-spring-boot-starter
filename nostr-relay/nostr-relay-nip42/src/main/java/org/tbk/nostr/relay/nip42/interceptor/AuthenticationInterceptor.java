package org.tbk.nostr.relay.nip42.interceptor;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrRequestContext;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

public interface AuthenticationInterceptor extends RequestHandlerInterceptor {

    default boolean preHandle(NostrRequestContext context, Request request) {
        if (request.getKindCase() != Request.KindCase.AUTH && request.getKindCase() != Request.KindCase.CLOSE) {
            if (!context.isAuthenticated()) {
                return preHandleUnauthenticated(context, request);
            }
        }

        return true;
    }

    boolean preHandleUnauthenticated(NostrRequestContext context, Request request);

}
