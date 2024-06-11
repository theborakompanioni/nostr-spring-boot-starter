package org.tbk.nostr.relay;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.interceptor.RequestHandlerInterceptor;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Handler execution chain, consisting of handler object and any handler interceptors.
 */
public class NostrRequestHandlerExecutionChain {

    private final List<RequestHandlerInterceptor> interceptors;

    public NostrRequestHandlerExecutionChain(List<RequestHandlerInterceptor> interceptors) {
        this.interceptors = Collections.unmodifiableList(requireNonNull(interceptors));
    }

    /**
     * Apply preHandle methods of registered interceptors.
     *
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, it is assumed
     * that this interceptor has already dealt with the response itself.
     */
    boolean applyPreHandle(NostrRequestContext context, Request request) throws Exception {
        for (RequestHandlerInterceptor interceptor : this.interceptors) {
            if (!interceptor.preHandle(context, request)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply postHandle methods of registered interceptors.
     */
    void applyHandle(NostrRequestContext context, Request request, NostrWebSocketHandler handler) throws Exception {
        switch (request.getKindCase()) {
            case EVENT -> handler.handleEventMessage(context, request.getEvent());
            case REQ -> handler.handleReqMessage(context, request.getReq());
            case CLOSE -> handler.handleCloseMessage(context, request.getClose());
            case COUNT -> handler.handleCountMessage(context, request.getCount());
            case AUTH -> handler.handleAuthMessage(context, request.getAuth());
            case KIND_NOT_SET -> handler.handleUnknownMessage(context, request);
        }
    }

    /**
     * Apply postHandle methods of registered interceptors.
     */
    void applyPostHandle(NostrRequestContext context, Request request) throws Exception {
        for (int i = this.interceptors.size() - 1; i >= 0; i--) {
            RequestHandlerInterceptor interceptor = this.interceptors.get(i);
            interceptor.postHandle(context, request);
        }
    }
}
