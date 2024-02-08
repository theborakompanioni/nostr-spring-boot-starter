package org.tbk.nostr.relay.example.domain.event;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Value;
import org.hibernate.annotations.CreationTimestamp;
import org.jmolecules.ddd.types.AggregateRoot;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

        try {
            this.tags = event.getTagsCount() == 0 ? null : JSON.std.composeString()
                    .addObject(listFromTags(event.getTagsList()))
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize tags", e);
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
                .setId(ByteString.fromHex(id.getId()))
                .setPubkey(ByteString.fromHex(this.pubkey))
                .setCreatedAt(this.createdAt.getEpochSecond())
                .setKind(this.kind)
                .addAllTags(Optional.ofNullable(tags)
                        .map(EventEntity::tagsFromJsonArray)
                        .map(EventEntity::tagsFromList)
                        .orElseGet(Collections::emptyList))
                .setContent(this.getContent())
                .setSig(ByteString.copyFrom(this.sig))
                .build();
    }

    @Value(staticConstructor = "of")
    public static class EventEntityId implements Identifier {
        public static EventEntityId create(Event event) {
            return EventEntityId.of(HexFormat.of().formatHex(event.getId().toByteArray()));
        }

        String id;
    }


    private static List<List<String>> tagsFromJsonArray(String json) {
        try {
            @SuppressWarnings("unchecked")
            List<List<String>> tags = (List<List<String>>) JSON.std.anyFrom(json);
            return tags;
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize tags", e);
        }
    }

    private static List<TagValue> tagsFromList(List<List<String>> tags) {
        return ImmutableList.<TagValue>builder()
                .addAll(tags.stream().filter(it -> !it.isEmpty())
                        .map(it -> TagValue.newBuilder()
                                .setName(it.getFirst())
                                .addAllValues(Iterables.skip(it, 1))
                                .build())
                        .toList())
                .build();
    }

    private static String tagsToJsonArray(List<TagValue> tags) {
        try {
            return JSON.std.composeString()
                    .addObject(listFromTags(tags))
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize tags", e);
        }
    }

    private static List<List<String>> listFromTags(List<TagValue> tags) {
        return tags.stream()
                .map(it -> Stream.concat(Stream.of(it.getName()), it.getValuesList().stream()).toList())
                .toList();
    }
}
