package org.tbk.nostr.example.relay.domain.event;

import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.proto.Filter;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

public final class EventEntitySpecifications {

    public static Specification<EventEntity> hasPubkey(XonlyPublicKey pubkey) {
        return (root, cq, cb) -> cb.equal(root.get("pubkey"), pubkey.value.toHex());
    }

    public static Specification<EventEntity> hasPubkey(ByteString pubkey) {
        return hasPubkey(new XonlyPublicKey(new ByteVector32(pubkey.toByteArray())));
    }

    public static Specification<EventEntity> hasKind(Kind kind) {
        return hasKind(kind.getValue());
    }

    public static Specification<EventEntity> matches(EventUri eventUri) {
        Specification<EventEntity> spec = hasPubkey(ByteString.fromHex(eventUri.getPublicKeyHex()))
                .and(hasKind(eventUri.getKind()));

        return eventUri.getIdentifier()
                .map(id -> spec.and(hasTagWithFirstValue(IndexedTag.d, id)))
                .orElse(spec);
    }

    public static Specification<EventEntity> hasKind(int kind) {
        return (root, cq, cb) -> cb.equal(root.get("kind"), kind);
    }

    public static Specification<EventEntity> hasId(EventId eventId) {
        return (root, cq, cb) -> cb.equal(root.get("id").get("id"), eventId.toHex());
    }

    public static Specification<EventEntity> hasId(ByteString eventId) {
        return hasId(EventId.of(eventId.toByteArray()));
    }

    public static Specification<EventEntity> isCreatedBefore(Instant now) {
        return (root, query, cb) -> cb.lessThan(root.get("createdAt"), now);
    }

    public static Specification<EventEntity> isCreatedBeforeInclusive(Instant now) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), now);
    }

    public static Specification<EventEntity> isCreatedAfter(Instant now) {
        return (root, query, cb) -> cb.greaterThan(root.get("createdAt"), now);
    }

    public static Specification<EventEntity> isCreatedAfterInclusive(Instant now) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), now);
    }

    public static Specification<EventEntity> isNotExpired() {
        return isNotExpired(Instant.now());
    }

    public static Specification<EventEntity> isNotExpired(Instant now) {
        return Specification.not(isExpired(now));
    }

    public static Specification<EventEntity> isExpired(Instant now) {
        return (root, query, cb) -> cb.and(cb.isNotNull(root.get("expiresAt")), cb.lessThan(root.get("expiresAt"), now));
    }

    public static Specification<EventEntity> isNotDeleted() {
        return Specification.not(isDeleted());
    }

    public static Specification<EventEntity> isDeleted() {
        return (root, cq, cb) -> cb.isNotNull(root.get("deletedAt"));
    }

    public static Specification<EventEntity> hasTagWithoutValues(IndexedTag tagName) {
        return hasTagWithFirstValue(tagName, null);
    }

    public static Specification<EventEntity> hasTagWithFirstValue(IndexedTag tagName, @Nullable String firstTagValue) {
        return (root, query, criteriaBuilder) -> {
            Join<TagEntity, EventEntity> eventTags = root.join("tags");
            return criteriaBuilder.and(
                    criteriaBuilder.equal(eventTags.get("name"), tagName.name()),
                    firstTagValue == null ? criteriaBuilder.isNull(eventTags.get("value0")) : criteriaBuilder.equal(eventTags.get("value0"), firstTagValue)
            );
        };
    }

    private static final Descriptors.FieldDescriptor untilFieldDescription = Filter.getDescriptor().findFieldByNumber(Filter.UNTIL_FIELD_NUMBER);
    private static final Descriptors.FieldDescriptor sinceFieldDescription = Filter.getDescriptor().findFieldByNumber(Filter.SINCE_FIELD_NUMBER);

    public static Specification<EventEntity> fromFilter(Filter filter) {
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
