package org.tbk.nostr.relay.example.domain.event;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Getter
@Table(name = "event")
public class EventEntity extends AbstractAggregateRoot<EventEntity> implements AggregateRoot<EventEntity, EventEntity.EventEntityId> {

    private final EventEntityId id;

    private final String pubkey;

    private final int kind;

    @Column(name = "created_at")
    private final Instant createdAt;

    @JoinColumn(name = "event_id", updatable = false)
    @OrderColumn(name = "position", updatable = false)
    private final List<TagEntity> tags = new ArrayList<>();

    private final String content;

    private final byte[] sig;

    @CreationTimestamp
    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    /**
     * Creates a new {@link EventEntity} for the given {@link Event}.
     *
     * @param event must not be {@literal null}.
     */
    EventEntity(Event event) {
        this.id = EventEntityId.create(event);
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

    public EventId asEventId() {
        return EventId.fromHex(this.id.getId());
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

    public EventEntity markDeleted() {
        return markDeleted(Instant.now());
    }


    public EventEntity markDeleted(Instant now) {
        this.deletedAt = now;
        registerEvent(new EventEntityEvents.MarkDeletedEvent(this.id));
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

    @Value(staticConstructor = "of")
    public static class EventEntityId implements Identifier {
        static EventEntityId create(Event event) {
            return EventEntityId.of(HexFormat.of().formatHex(event.getId().toByteArray()));
        }

        @NonNull
        String id;
    }
}
