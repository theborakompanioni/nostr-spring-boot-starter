package org.tbk.nostr.util;

import com.google.common.collect.Range;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.Kinds;

import java.util.function.Predicate;

public final class MoreKinds {
    private static final Range<Kind> REPLACEABLE_KIND_RANGE = Range.closedOpen(
            Kind.of(10_000),
            Kind.of(20_000)
    );

    private static final Predicate<Kind> REPLACEABLE_KIND_PREDICATE = REPLACEABLE_KIND_RANGE
            .or(Kinds.kindProfileMetadata::equals)
            .or(Kinds.kindFollowList::equals);

    private static final Range<Kind> EPHEMERAL_KIND_RANGE = Range.closedOpen(
            REPLACEABLE_KIND_RANGE.upperEndpoint(),
            Kind.of(30_000)
    );

    private static final Range<Kind> ADDRESSABLE_KIND_RANGE = Range.closedOpen(
            EPHEMERAL_KIND_RANGE.upperEndpoint(),
            Kind.of(40_000)
    );

    private MoreKinds() {
        throw new UnsupportedOperationException();
    }

    public static Range<Kind> kindReplaceableRange() {
        return REPLACEABLE_KIND_RANGE;
    }

    public static Range<Kind> kindEphemeralRange() {
        return EPHEMERAL_KIND_RANGE;
    }

    public static Range<Kind> kindAddressableRange() {
        return ADDRESSABLE_KIND_RANGE;
    }

    public static boolean isReplaceable(int kind) {
        return REPLACEABLE_KIND_PREDICATE.test(Kind.of(kind));
    }

    public static void checkReplaceable(int kind) {
        if (!isReplaceable(kind)) {
            throw new IllegalArgumentException("Given kind is not replaceable. Must be %d, %d, or %d <= n < %d, got: %d".formatted(
                    Kinds.kindProfileMetadata.getValue(),
                    Kinds.kindFollowList.getValue(),
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

    public static boolean isAddressable(int kind) {
        return ADDRESSABLE_KIND_RANGE.contains(Kind.of(kind));
    }

    public static void checkAddressable(int kind) {
        if (!isAddressable(kind)) {
            throw new IllegalArgumentException("Given kind is not addressable. Must be %d <= n < %d, got: %d".formatted(
                    ADDRESSABLE_KIND_RANGE.lowerEndpoint().getValue(),
                    ADDRESSABLE_KIND_RANGE.upperEndpoint().getValue(),
                    kind
            ));
        }
    }
}
