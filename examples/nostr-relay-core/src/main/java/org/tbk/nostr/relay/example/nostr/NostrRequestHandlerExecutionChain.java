package org.tbk.nostr.relay.example.nostr;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.example.nostr.interceptor.NostrRequestHandlerInterceptor;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Handler execution chain, consisting of handler object and any handler interceptors.
 */
public class NostrRequestHandlerExecutionChain {

    private final List<NostrRequestHandlerInterceptor> interceptors;

    public NostrRequestHandlerExecutionChain(List<NostrRequestHandlerInterceptor> interceptors) {
        this.interceptors = Collections.unmodifiableList(requireNonNull(interceptors));
    }

    /**
     * Apply preHandle methods of registered interceptors.
     *
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, it is assumed
     * that this interceptor has already dealt with the response itself.
     */
    boolean applyPreHandle(NostrWebSocketSession session, Request request) throws Exception {
        for (NostrRequestHandlerInterceptor interceptor : this.interceptors) {
            if (!interceptor.preHandle(session, request)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Apply postHandle methods of registered interceptors.
     */
    void applyHandle(NostrWebSocketSession session, Request request, NostrWebSocketHandler handler) throws Exception {
        switch (request.getKindCase()) {
            case EVENT -> handler.handleEventMessage(session, request.getEvent());
            case REQ -> handler.handleReqMessage(session, request.getReq());
            case CLOSE -> handler.handleCloseMessage(session, request.getClose());
            case COUNT -> handler.handleCountMessage(session, request.getCount());
            case KIND_NOT_SET -> handler.handleUnknownMessage(session, request);
        }
    }

    /**
     * Apply postHandle methods of registered interceptors.
     */
    void applyPostHandle(NostrWebSocketSession session, Request request) throws Exception {
        for (int i = this.interceptors.size() - 1; i >= 0; i--) {
            NostrRequestHandlerInterceptor interceptor = this.interceptors.get(i);
            interceptor.postHandle(session, request);
        }
    }

}
