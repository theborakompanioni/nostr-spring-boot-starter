/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.tbk.nostr.relay.example.nostr.interceptor;

import org.tbk.nostr.proto.Request;
import org.tbk.nostr.relay.example.nostr.NostrWebSocketSession;

/**
 * Workflow interface that allows for customized handler execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 */
public interface NostrRequestHandlerInterceptor {

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
