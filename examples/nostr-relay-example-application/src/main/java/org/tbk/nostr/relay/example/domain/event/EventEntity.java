package org.tbk.nostr.relay.example.domain.event;

import com.fasterxml.jackson.jr.ob.JSON;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;

import java.io.IOException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

/**
 * An order.
 */
@Getter
@Table(name = "event")
public class EventEntity extends AbstractAggregateRoot<EventEntity> implements AggregateRoot<EventEntity, EventEntity.EventEntityId> {

    private final EventEntityId id;

    private final String pubkey;

    private final int kind;

    @Column(name = "created_at")
    private final Instant createdAt;

    private final String tags;

    private final String content;

    private final byte[] sig;

    private Integer deleted;

    @CreationTimestamp
    @Column(name = "first_seen_at")
    private Instant firstSeenAt;

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

        try {
            this.tags = event.getTagsCount() == 0 ? null : JSON.std.composeString()
                    .addObject(listFromTags(event.getTagsList()))
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize tags", e);
        }

        registerEvent(new EventEntityEvents.CreatedEvent(this.id));
    }

    public boolean isDeleted() {
        return deleted == 1;
    }

    public boolean isExpired(Instant now) {
        return expiresAt != null && expiresAt.isBefore(now);
    }

    public EventEntity markDeleted() {
        this.deleted = 1;
        registerEvent(new EventEntityEvents.MarkDeletedEvent(this.id));
        return this;
    }

    @Value(staticConstructor = "of")
    public static class EventEntityId implements Identifier {
        public static EventEntityId create(Event event) {
            return EventEntityId.of(HexFormat.of().formatHex(event.getId().toByteArray()));
        }

        String id;
    }

    private static List<List<String>> listFromTags(List<TagValue> tags) {
        return tags.stream()
                .map(it -> Stream.concat(Stream.of(it.getName()), it.getValuesList().stream()).toList())
                .toList();
    }
}