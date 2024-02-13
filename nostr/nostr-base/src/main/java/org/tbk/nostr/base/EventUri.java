package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.HexFormat;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

// "coordinates to an event" in the form of:
// - "<kind integer>:<32-bytes lowercase hex of a pubkey>" for non-parameterized replaceable events
// - "<kind integer>:<32-bytes lowercase hex of a pubkey>:<d tag value>" for parameterized replaceable events
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventUri {
    public static EventUri fromString(String uri) {
        return new EventUri(uri);
    }

    public static EventUri of(int kind, String publicKeyHex) {
        return of(Kind.of(kind), publicKeyHex);
    }

    public static EventUri of(Kind kind, @NonNull String publicKeyHex) {
        return new EventUri("%d:%s".formatted(kind.getValue(), publicKeyHex));
    }

    public static EventUri of(Kind kind, @NonNull String publicKeyHex, @NonNull String identifier) {
        return new EventUri("%d:%s:%s".formatted(kind.getValue(), publicKeyHex, identifier));
    }

    public static boolean isValidEventUriString(String value) {
        String[] split = value.split(":", 3);
        if (split.length < 2) {
            return false;
        }
        if (!Kind.isValidKindString(split[0])) {
            return false;
        }
        if (!looksLikePublicKey(split[1])) {
            return false;
        }
        return true;
    }

    @EqualsAndHashCode.Include
    @Getter
    @NonNull
    private final Kind kind;


    @EqualsAndHashCode.Include
    @Getter
    @NonNull
    private final String publicKeyHex;

    @EqualsAndHashCode.Include
    @Nullable
    private final String identifier;

    private EventUri(String value) {
        requireNonNull(value);
        if (!isValidEventUriString(value)) {
            throw new IllegalArgumentException("Invalid argument given, expected valid event uri, got: %s".formatted(value));
        }
        String[] split = value.split(":", 3);
        this.kind = Kind.fromString(split[0]);
        this.publicKeyHex = split[1];

        this.identifier = split.length != 3 ? null : split[2];
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    public URI toUri() {
        return URI.create(this.toString());
    }

    @Override
    public String toString() {
        return identifier == null ?
                "%d:%s".formatted(kind.getValue(), publicKeyHex) :
                "%d:%s:%s".formatted(kind.getValue(), publicKeyHex, identifier);
    }

    private static boolean looksLikePublicKey(String value) {
        if (value.length() != 64) {
            return false;
        } else {
            try {
                return HexFormat.of().parseHex(value).length == 32;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
