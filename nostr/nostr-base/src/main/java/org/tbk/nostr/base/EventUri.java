package org.tbk.nostr.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import javax.annotation.Nullable;
import java.util.HexFormat;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

// "coordinates to an event" in the form of:
// - "<kind integer>:<32-bytes lowercase hex of a pubkey>" for non-parameterized replaceable events
// - "<kind integer>:<32-bytes lowercase hex of a pubkey>:<d tag value>" for parameterized replaceable events
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public final class EventUri {

    public static boolean isValidEventUriString(String value) {
        try {
            fromString(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static EventUri of(int kind, String publicKeyHex) {
        return of(Kind.of(kind), publicKeyHex);
    }

    public static EventUri of(Kind kind, @NonNull String publicKeyHex) {
        return fromString("%d:%s".formatted(kind.getValue(), publicKeyHex));
    }

    public static EventUri of(int kind, @NonNull String publicKeyHex, @NonNull String identifier) {
        return of(Kind.of(kind), publicKeyHex, identifier);
    }

    public static EventUri of(Kind kind, @NonNull String publicKeyHex, @NonNull String identifier) {
        return fromString("%d:%s:%s".formatted(kind.getValue(), publicKeyHex, identifier));
    }

    public static EventUri fromString(String value) {
        try {
            String[] split = value.split(":", 3);
            if (split.length < 2) {
                throw new IllegalArgumentException("Invalid string.");
            }
            String kindString = split[0];
            String supposedPublicKey = split[1];

            if (supposedPublicKey.length() != 64) {
                throw new IllegalArgumentException("Invalid pubkey string.");
            }

            String identifier = split.length != 3 ? null : split[2];
            return new EventUri(Kind.fromString(kindString), supposedPublicKey, identifier);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while parsing event uri: " + e.getMessage());
        }
    }

    @EqualsAndHashCode.Include
    @Getter
    @NonNull
    private final Kind kind;

    @EqualsAndHashCode.Include
    @Getter
    private final byte[] publicKey;

    @EqualsAndHashCode.Include
    @Nullable
    private final String identifier;

    private EventUri(Kind kind, String publicKeyHex, @Nullable String identifier) {
        byte[] publicKey = parsePublicKey(publicKeyHex);
        if (!looksLikePublicKey(publicKey)) {
            throw new IllegalArgumentException("Invalid pubkey.");
        }
        this.kind = requireNonNull(kind);
        this.publicKey = publicKey;
        this.identifier = identifier;
    }

    public Optional<String> getIdentifier() {
        return Optional.ofNullable(identifier);
    }

    public String getPublicKeyHex() {
        return HexFormat.of().formatHex(this.publicKey);
    }

    @Override
    public String toString() {
        return identifier == null ?
                "%d:%s".formatted(kind.getValue(), getPublicKeyHex()) :
                "%d:%s:%s".formatted(kind.getValue(), getPublicKeyHex(), identifier);
    }

    private static boolean looksLikePublicKey(byte[] raw) {
        return raw.length == 32;
    }

    private static byte[] parsePublicKey(String publicKeyHex) {
        try {
            return HexFormat.of().parseHex(publicKeyHex);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid pubkey.");
        }
    }
}
