package org.tbk.nostr.example.relay.domain.event;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.example.relay.db.converter.MoreConverter;
import org.tbk.nostr.proto.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Getter
@Table(name = "event")
public class EventEntity extends AbstractAggregateRoot<EventEntity> implements AggregateRoot<EventEntity, EventEntity.EventEntityId> {

    private final EventEntityId id;

    @Column(name = "pubkey", nullable = false, updatable = false)
    private final String pubkey;

    @Column(name = "kind", nullable = false, updatable = false)
    private final int kind;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Convert(converter = MoreConverter.InstantToSecondsConverter.class)
    private final Instant createdAt;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", updatable = false)
    @OrderColumn(name = "position", updatable = false)
    private final List<TagEntity> tags = new ArrayList<>();

    @Column(name = "content", nullable = false, updatable = false)
    private final String content;

    @Column(name = "sig", nullable = false, updatable = false)
    private final byte[] sig;

    @CreationTimestamp
    @Column(name = "first_seen_at")
    @Convert(converter = MoreConverter.InstantToMilliSecondsConverter.class)
    private Instant firstSeenAt;

    @Column(name = "deleted_at")
    @Convert(converter = MoreConverter.InstantToMilliSecondsConverter.class)
    private Instant deletedAt;

    @Column(name = "expires_at")
    @Convert(converter = MoreConverter.InstantToMilliSecondsConverter.class)
    private Instant expiresAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_id", updatable = false)
    private final List<EventNip50MetaInfoEntity> nip50MetaInfos = new ArrayList<>();

    /**
     * Creates a new {@link EventEntity} for the given {@link Event}.
     *
     * @param event must not be {@literal null}.
     */
    EventEntity(Event event) {
        this.id = EventEntityId.fromEvent(event);
        this.pubkey = HexFormat.of().formatHex(event.getPubkey().toByteArray());
        this.kind = event.getKind();
        this.createdAt = Instant.ofEpochSecond(event.getCreatedAt());
        this.content = event.getContent();
        this.sig = event.getSig().toByteArray();

        for (int i = 0; i < event.getTagsCount(); i++) {
            this.tags.add(new TagEntity(event.getTags(i), this.id, i));
        }

        registerEvent(new EventEntityEvents.CreatedEvent(this.id));
    }

    public XonlyPublicKey asPublicKey() {
        return new XonlyPublicKey(ByteVector32.fromValidHex(this.pubkey));
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    EventEntity markDeleted(Instant now) {
        this.deletedAt = now;
        registerEvent(new EventEntityEvents.MarkDeletedEvent(this.id));
        return this;
    }

    EventEntity markExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
        return this;
    }

    public Event toNostrEvent() {
        return Event.newBuilder()
                .setId(ByteString.fromHex(this.id.getId()))
                .setPubkey(ByteString.fromHex(this.pubkey))
                .setCreatedAt(this.createdAt.getEpochSecond())
                .setKind(this.kind)
                .addAllTags(this.tags.stream()
                        .map(TagEntity::toNostrTag)
                        .toList())
                .setContent(this.getContent())
                .setSig(ByteString.copyFrom(this.sig))
                .build();
    }

    EventEntity addNip50MetaInfo(EventNip50MetaInfoEntity nip50MetaInfo) {
        nip50MetaInfos.add(nip50MetaInfo);
        return this;
    }

    @Value(staticConstructor = "of")
    public static class EventEntityId implements Identifier {
        public static EventEntityId fromEvent(Event event) {
            return EventEntityId.of(HexFormat.of().formatHex(event.getId().toByteArray()));
        }

        @NonNull
        String id;

        public EventId toEventId() {
            return EventId.fromHex(this.id);
        }
    }
}
