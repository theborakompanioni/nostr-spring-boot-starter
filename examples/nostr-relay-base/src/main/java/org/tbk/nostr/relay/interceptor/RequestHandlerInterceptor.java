package org.tbk.nostr.relay.interceptor;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.NostrWebSocketSession;

/**
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 */
public interface RequestHandlerInterceptor {

    /**
     * Interception point before the execution of a handler.
     * <p>The default implementation returns {@code true}.
     *
     * @param session websocket session
     * @param request current request
     * @return {@code true} if the execution chain should proceed with the
     * next interceptor or the handler itself. Else, it is assumed
     * that this interceptor has already dealt with the response itself.
     * @throws Exception in case of errors
     */
    default boolean preHandle(NostrWebSocketSession session, Request request) throws Exception {
        return true;
    }

    /**
     * Interception point after successful execution of a handler.
     * Called after HandlerAdapter actually invoked the handler.
     * <p>The default implementation is empty.
     *
     * @param session websocket session
     * @param request current request
     * @throws Exception in case of errors
     */
    default void postHandle(NostrWebSocketSession session, Request request) throws Exception {
    }

}
