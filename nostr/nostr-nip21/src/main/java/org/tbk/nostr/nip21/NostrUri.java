package org.tbk.nostr.nip21;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nip19.Nip19Entity;

import java.net.URI;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public final class NostrUri {
    private static final String SCHEME = "nostr";

    public static boolean isValidNostrUriString(String value) {
        return tryFromString(value).isPresent();
    }

    public static NostrUri fromString(String uri) {
        return of(URI.create(uri));
    }

    public static Optional<NostrUri> tryFromString(String uri) {
        try {
            return Optional.of(fromString(uri));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static NostrUri of(Nip19Entity entity) {
        return switch (entity.getEntityType()) {
            case NSEC -> throw new IllegalArgumentException("Unsupported value: %s".formatted(entity.getEntityType()));
            default -> new NostrUri(entity);
        };
    }

    public static NostrUri of(URI uri) {
        if (!SCHEME.equalsIgnoreCase(uri.getScheme())) {
            throw new IllegalArgumentException("Unsupported scheme. Expected '%s', got: %s.".formatted(SCHEME, uri.getScheme()));
        }
        return of(Nip19.decode(uri.getSchemeSpecificPart()));
    }

    @Getter
    private final Nip19Entity entity;

    @Getter
    @EqualsAndHashCode.Include
    @ToString.Include
    private final URI uri;

    private NostrUri(Nip19Entity entity) {
        this.entity = requireNonNull(entity);
        this.uri = URI.create("%s:%s".formatted(SCHEME, Nip19.encode(entity)));
    }
}
