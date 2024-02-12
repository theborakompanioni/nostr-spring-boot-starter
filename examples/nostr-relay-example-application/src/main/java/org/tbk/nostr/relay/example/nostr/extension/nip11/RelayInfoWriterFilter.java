package org.tbk.nostr.relay.example.nostr.extension.nip11;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.tbk.nostr.nip11.RelayInfoDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import static java.util.Objects.requireNonNull;


class RelayInfoWriterFilter implements Filter {
    private static final String APPLICATION_JSON_NOSTR_VALUE = new MediaType("application", "nostr+json").toString();

    private final String path;

    private final String json;
    private final int contentLength;

    public RelayInfoWriterFilter(String path, RelayInfoDocument relayInfoDocument) {
        this.path = requireNonNull(path);
        this.json = requireNonNull(relayInfoDocument).toJson();
        this.contentLength = this.json.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if (servletRequest instanceof HttpServletRequest httpServletRequest &&
            servletResponse instanceof HttpServletResponse httpServletResponse) {
            if (isRelayInfoDocumentRequest(httpServletRequest)) {
                writeRelayInfoDocument(httpServletResponse);
                return;
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void writeRelayInfoDocument(HttpServletResponse response) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setContentLength(contentLength);
        response.getWriter().write(json);
    }

    private boolean isRelayInfoDocumentRequest(HttpServletRequest request) {
        boolean isGetRequest = HttpMethod.GET.name().equalsIgnoreCase(request.getMethod());
        if (!isGetRequest) {
            return false;
        }
        boolean isWebSocketHandshakeRequest = isWebSocketHandshakeRequest(request);
        if (isWebSocketHandshakeRequest) {
            return false;
        }
        boolean isWebsocketPath = matches(request, this.path);
        if (!isWebsocketPath) {
            return false;
        }
        boolean hasHeader = hasHeaderWithValueIgnoreCase(request, HttpHeaders.ACCEPT, APPLICATION_JSON_NOSTR_VALUE);
        if (!hasHeader) {
            return false;
        }

        return true;
    }

    private boolean isWebSocketHandshakeRequest(HttpServletRequest request) {
        return hasHeaderWithValueIgnoreCase(request, HttpHeaders.UPGRADE, "websocket");
    }

    private boolean hasHeaderWithValueIgnoreCase(HttpServletRequest request, String requiredName, String requiredValue) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                if (requiredName.equalsIgnoreCase(headerNames.nextElement())) {
                    Enumeration<String> acceptHeaderValues = request.getHeaders(requiredName);
                    while (acceptHeaderValues.hasMoreElements()) {
                        if (requiredValue.equalsIgnoreCase(acceptHeaderValues.nextElement())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean matches(HttpServletRequest request, String url) {
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
            return uri.equals(url);
        }
        return uri.equals(request.getContextPath() + url);
    }
}
