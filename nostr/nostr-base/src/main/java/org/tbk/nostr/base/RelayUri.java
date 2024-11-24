package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class RelayUri {

    public static boolean isValidRelayUriString(String value) {
        return tryParse(value).isPresent();
    }

    public static RelayUri parse(String uri) {
        return of(URI.create(uri));
    }

    public static Optional<RelayUri> tryParse(String uri) {
        try {
            return Optional.of(parse(uri));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

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
