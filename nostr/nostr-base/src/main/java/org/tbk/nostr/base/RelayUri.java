package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class RelayUri {

    public static RelayUri of(URI uri) {
        return new RelayUri(uri);
    }

    @EqualsAndHashCode.Include
    @ToString.Include
    private final URI uri;

    private RelayUri(URI uri) {
        requireNonNull(uri);
        if (!"ws".equalsIgnoreCase(uri.getScheme()) && !"wss".equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Unsupported scheme");
        }
        this.uri = uri;
    }

    public URI getUri() {
        return this.uri;
    }
}
