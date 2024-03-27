package org.tbk.nostr.relay.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;

import java.util.Enumeration;
import java.util.function.Predicate;

public final class MoreHttpRequests {

    private MoreHttpRequests() {
        throw new UnsupportedOperationException();
    }

    public static boolean isWebSocketHandshakeRequest(HttpServletRequest request) {
        return headerMatches(request, HttpHeaders.UPGRADE, "websocket"::equalsIgnoreCase);
    }

    public static boolean headerMatches(HttpServletRequest request, String headerName, Predicate<String> matcher) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                if (headerName.equalsIgnoreCase(headerNames.nextElement())) {
                    Enumeration<String> acceptHeaderValues = request.getHeaders(headerName);
                    while (acceptHeaderValues.hasMoreElements()) {
                        if (matcher.test(acceptHeaderValues.nextElement())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public static boolean requestUriMatches(HttpServletRequest request, @NonNull String expectedValue) {
        return requestUriMatches(request, expectedValue::equalsIgnoreCase);
    }

    public static boolean requestUriMatches(HttpServletRequest request, Predicate<String> expectedValueMatcher) {
        String uri = request.getRequestURI();
        int pathParamIndex = uri.indexOf(';');
        if (pathParamIndex > 0) {
            // strip everything after the first semicolon
            uri = uri.substring(0, pathParamIndex);
        }
        if (request.getQueryString() != null) {
            uri += "?" + request.getQueryString();
        }
        if ("".equals(request.getContextPath())) {
            return expectedValueMatcher.test(uri);
        }
        return expectedValueMatcher.test(uri.substring(0, uri.lastIndexOf(request.getContextPath())));
    }
}
