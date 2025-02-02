package org.tbk.nostr.relay.plugin.allowlist.db.domain;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.relay.plugin.allowlist.db.converter.MoreConverter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Table(name = "plugin_allowlist_entry")
public class AllowlistEntry extends AbstractAggregateRoot<AllowlistEntry> implements AggregateRoot<AllowlistEntry, AllowlistEntry.AllowlistEntryId> {

    private final AllowlistEntryId id;

    @Column(name = "pubkey", nullable = false, updatable = false)
    private final String pubkey;

    @CreationTimestamp
    @Column(name = "created_at")
    @Convert(converter = MoreConverter.InstantToMilliSecondsConverter.class)
    private Instant createdAt;

    @Column(name = "expires_at")
    @Convert(converter = MoreConverter.InstantToMilliSecondsConverter.class)
    private Instant expiresAt;

    /**
     * Creates a new {@link AllowlistEntry} for the given {@link XonlyPublicKey}.
     *
     * @param pubkey must not be {@literal null}.
     */
    AllowlistEntry(XonlyPublicKey pubkey) {
        this.id = AllowlistEntryId.create();
        this.pubkey = pubkey.value.toHex();
    }

    public XonlyPublicKey asPublicKey() {
        return new XonlyPublicKey(ByteVector32.fromValidHex(this.pubkey));
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    AllowlistEntry markExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    @Value(staticConstructor = "of")
    public static class AllowlistEntryId implements Identifier {
        public static AllowlistEntryId create() {
            return AllowlistEntryId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;

        public EventId toEventId() {
            return EventId.fromHex(this.id);
        }
    }
}
