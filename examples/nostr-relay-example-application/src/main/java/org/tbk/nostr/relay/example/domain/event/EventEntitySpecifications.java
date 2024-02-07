package org.tbk.nostr.relay.example.domain.event;

import fr.acinq.bitcoin.XonlyPublicKey;
import org.springframework.data.jpa.domain.Specification;
import org.tbk.nostr.base.EventId;

import java.time.Instant;

final class EventEntitySpecifications {
    static Specification<EventEntity> hasPubkey(XonlyPublicKey pubkey) {
        return (event, cq, cb) -> cb.equal(event.get("pubkey"), pubkey.value.toHex());
    }

    static Specification<EventEntity> hasKind(int kind) {
        return (event, cq, cb) -> cb.equal(event.get("kind"), kind);
    }

    static Specification<EventEntity> hasId(EventId eventId) {
        return (event, cq, cb) -> cb.equal(event.get("id").get("id"), eventId.toHex());
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
}
