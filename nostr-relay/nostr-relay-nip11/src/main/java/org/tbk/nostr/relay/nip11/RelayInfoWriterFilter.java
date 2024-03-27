package org.tbk.nostr.relay.nip11;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.relay.utils.MoreHttpRequests;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;


public class RelayInfoWriterFilter implements Filter {
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
        if (MoreHttpRequests.isWebSocketHandshakeRequest(request)) {
            return false;
        }
        boolean isWebsocketPath = MoreHttpRequests.requestUriMatches(request, this.path);
        if (!isWebsocketPath) {
            return false;
        }
        boolean hasExpectedHeader = MoreHttpRequests.headerMatches(request, HttpHeaders.ACCEPT, APPLICATION_JSON_NOSTR_VALUE::equalsIgnoreCase);
        if (!hasExpectedHeader) {
            return false;
        }

        return true;
    }
}
