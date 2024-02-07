package org.tbk.nostr.relay.example.domain.event;

import com.google.protobuf.Descriptors;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.jpa.domain.Specification;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Filter;

import java.time.Instant;
import java.util.Optional;

final class EventEntitySpecifications {

    static Specification<EventEntity> hasPubkey(XonlyPublicKey pubkey) {
        return (root, cq, cb) -> cb.equal(root.get("pubkey"), pubkey.value.toHex());
    }

    static Specification<EventEntity> hasKind(int kind) {
        return (root, cq, cb) -> cb.equal(root.get("kind"), kind);
    }

    static Specification<EventEntity> hasId(EventId eventId) {
        return (root, cq, cb) -> cb.equal(root.get("id").get("id"), eventId.toHex());
    }

    static Specification<EventEntity> isCreatedBeforeInclusive(Instant now) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), now);
    }

    static Specification<EventEntity> isCreatedAfterInclusive(Instant now) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), now);
    }

    static Specification<EventEntity> isNotExpired() {
        return isNotExpired(Instant.now());
    }

    static Specification<EventEntity> isNotExpired(Instant now) {
        return Specification.not(isExpired(now));
    }

    private static Specification<EventEntity> isExpired(Instant now) {
        return (root, query, cb) -> cb.and(cb.isNotNull(root.get("expiresAt")), cb.lessThan(root.get("expiresAt"), now));
    }

    static Specification<EventEntity> isNotDeleted() {
        return Specification.not(isDeleted());
    }

    private static Specification<EventEntity> isDeleted() {
        return (root, cq, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    private static final Descriptors.FieldDescriptor untilFieldDescription = Filter.getDescriptor().findFieldByNumber(Filter.UNTIL_FIELD_NUMBER);
    private static final Descriptors.FieldDescriptor sinceFieldDescription = Filter.getDescriptor().findFieldByNumber(Filter.SINCE_FIELD_NUMBER);

    static Specification<EventEntity> fromFilter(Filter filter) {
        Specification<EventEntity> idsSpecification = Specification.anyOf(filter.getIdsList().stream()
                .map(it -> EventId.of(it.toByteArray()))
                .map(EventEntitySpecifications::hasId)
                .toList());

        Specification<EventEntity> authorsSpecification = Specification.anyOf(filter.getAuthorsList().stream()
                .map(it -> new XonlyPublicKey(new ByteVector32(it.toByteArray())))
                .map(EventEntitySpecifications::hasPubkey)
                .toList());

        Specification<EventEntity> kindsSpecification = Specification.anyOf(filter.getKindsList().stream()
                .map(EventEntitySpecifications::hasKind)
                .toList());

        Specification<EventEntity> sinceSpecification = Optional.of(filter)
                .filter(it -> it.hasField(sinceFieldDescription))
                .map(Filter::getSince)
                .map(Instant::ofEpochSecond)
                .map(EventEntitySpecifications::isCreatedAfterInclusive)
                .orElseGet(() -> Specification.where(null));

        Specification<EventEntity> untilSpecification = Optional.of(filter)
                .filter(it -> it.hasField(untilFieldDescription))
                .map(Filter::getUntil)
                .map(Instant::ofEpochSecond)
                .map(EventEntitySpecifications::isCreatedBeforeInclusive)
                .orElseGet(() -> Specification.where(null));

        return Specification.allOf(
                idsSpecification,
                authorsSpecification,
                kindsSpecification,
                sinceSpecification,
                untilSpecification
        );
    }
}
