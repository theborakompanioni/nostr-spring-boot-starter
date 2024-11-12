package org.tbk.nostr.util;

import com.google.common.collect.Range;
import org.tbk.nostr.base.Kind;

import java.util.function.Predicate;

public final class MoreKinds {
    private static final Kind KIND_USER_METADATA = Kind.of(0);
    private static final Kind SHORT_TEXT_NOTE_KIND = Kind.of(1);
    private static final Kind KIND_FOLLOWS = Kind.of(3);
    private static final Kind REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE = Kind.of(10_000);
    private static final Kind REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE = Kind.of(20_000);
    private static final Range<Kind> REPLACEABLE_KIND_RANGE = Range.closedOpen(
            REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE,
            REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE
    );

    private static final Predicate<Kind> REPLACEABLE_KIND_PREDICATE = REPLACEABLE_KIND_RANGE
            .or(KIND_USER_METADATA::equals)
            .or(KIND_FOLLOWS::equals);

    private static final Kind EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE = REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE;
    private static final Kind EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE = Kind.of(30_000);
    private static final Range<Kind> EPHEMERAL_KIND_RANGE = Range.closedOpen(
            EPHEMERAL_KIND_LOWER_BOUND_INCLUSIVE,
            EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE
    );
    private static final Kind PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE = EPHEMERAL_KIND_UPPER_BOUND_EXCLUSIVE;
    private static final Kind PARAMETERIZED_REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE = Kind.of(40_000);
    private static final Range<Kind> PARAMETERIZED_REPLACEABLE_KIND_RANGE = Range.closedOpen(
            PARAMETERIZED_REPLACEABLE_KIND_LOWER_BOUND_INCLUSIVE,
            PARAMETERIZED_REPLACEABLE_KIND_UPPER_BOUND_EXCLUSIVE
    );

    private MoreKinds() {
        throw new UnsupportedOperationException();
    }

    public static Kind kindUserMetadata() {
        return KIND_USER_METADATA;
    }

    public static Kind kindShortTextNote() {
        return SHORT_TEXT_NOTE_KIND;
    }

    public static Kind kindFollows() {
        return KIND_FOLLOWS;
    }


    public static Range<Kind> kindReplaceableRange() {
        return REPLACEABLE_KIND_RANGE;
    }

    public static Range<Kind> kindEphemeralRange() {
        return EPHEMERAL_KIND_RANGE;
    }

    public static Range<Kind> kindParameterizedReplaceableRange() {
        return PARAMETERIZED_REPLACEABLE_KIND_RANGE;
    }

    public static boolean isReplaceable(int kind) {
        return REPLACEABLE_KIND_PREDICATE.test(Kind.of(kind));
    }

    public static void checkReplaceable(int kind) {
        if (!isReplaceable(kind)) {
            throw new IllegalArgumentException("Given kind is not replaceable. Must be %d, %d, or %d <= n < %d, got: %d".formatted(
                    KIND_USER_METADATA.getValue(),
                    KIND_FOLLOWS.getValue(),
                    REPLACEABLE_KIND_RANGE.lowerEndpoint().getValue(),
                    REPLACEABLE_KIND_RANGE.upperEndpoint().getValue(),
                    kind
            ));
        }
    }

    public static boolean isEphemeral(int kind) {
        return EPHEMERAL_KIND_RANGE.contains(Kind.of(kind));
    }

    public static void checkEphemeral(int kind) {
        if (!isEphemeral(kind)) {
            throw new IllegalArgumentException("Given kind is not ephemeral. Must be %d <= n < %d, got: %d".formatted(
                    EPHEMERAL_KIND_RANGE.lowerEndpoint().getValue(),
                    EPHEMERAL_KIND_RANGE.upperEndpoint().getValue(),
                    kind
            ));
        }
    }

    public static boolean isParameterizedReplaceable(int kind) {
        return PARAMETERIZED_REPLACEABLE_KIND_RANGE.contains(Kind.of(kind));
    }

    public static void checkParameterizedReplaceable(int kind) {
        if (!isParameterizedReplaceable(kind)) {
            throw new IllegalArgumentException("Given kind is not parameterized replaceable. Must be %d <= n < %d, got: %d".formatted(
                    PARAMETERIZED_REPLACEABLE_KIND_RANGE.lowerEndpoint().getValue(),
                    PARAMETERIZED_REPLACEABLE_KIND_RANGE.upperEndpoint().getValue(),
                    kind
            ));
        }
    }
}
