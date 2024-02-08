package org.tbk.nostr.relay.example.domain.event;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.protobuf.ProtocolStringList;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.tbk.nostr.proto.TagValue;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Getter
@Table(name = "event_tag", uniqueConstraints = {
        @UniqueConstraint(name = "", columnNames = {"event_id", "position"})
})
public class TagEntity implements Entity<EventEntity, TagEntity.TagEntityId> {

    @NonNull
    private final TagEntityId id;

    @NonNull
    @Column(name = "event_id", nullable = false, updatable = false)
    private final String eventId;

    @Column(name = "position", updatable = false)
    private final int position;

    @NonNull
    @Column(name = "name", nullable = false, updatable = false)
    private final String name;

    @Nullable
    @Column(name = "value0", updatable = false)
    private final String value0;

    @Nullable
    @Column(name = "value1", updatable = false)
    private final String value1;

    @Nullable
    @Column(name = "value2", updatable = false)
    private final String value2;

    @Nullable
    @Column(name = "other_values", updatable = false)
    private final String otherValues;

    /**
     * Creates a new {@link TagEntity} for the given {@link TagValue}.
     *
     * @param tag must not be {@literal null}.
     */
    TagEntity(TagValue tag, EventEntity.EventEntityId eventId, int position) {
        this.id = TagEntityId.create();
        this.eventId = eventId.getId();
        this.position = position;
        this.name = tag.getName();

        ProtocolStringList values = tag.getValuesList();
        this.value0 = values.stream().skip(0).findFirst().orElse(null);
        this.value1 = values.stream().skip(1).findFirst().orElse(null);
        this.value2 = values.stream().skip(2).findFirst().orElse(null);
        this.otherValues = tagsToJsonArray(values.stream().skip(3).toList());
    }

    public TagValue toNostrTag() {
        return TagValue.newBuilder()
                .setName(this.name)
                .addAllValues(
                        Stream.concat(
                                        Stream.of(value0, value1, value2),
                                        tagsFromJsonArray(otherValues).stream()
                                )
                                .filter(Objects::nonNull)
                                .toList()
                )
                .build();
    }

    @Value(staticConstructor = "of")
    public static class TagEntityId implements Identifier {

        static TagEntityId create() {
            return TagEntityId.of(UUID.randomUUID().toString());
        }

        @NonNull
        String id;
    }

    private static List<String> tagsFromJsonArray(@Nullable String json) {
        if (json == null) {
            return Collections.emptyList();
        }

        try {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) JSON.std.anyFrom(json);
            return tags;
        } catch (IOException e) {
            throw new RuntimeException("Could not deserialize tags", e);
        }
    }

    private static @Nullable String tagsToJsonArray(List<String> tags) {
        if (tags.isEmpty()) {
            return null;
        }

        try {
            return JSON.std.composeString()
                    .addObject(tags)
                    .finish();
        } catch (IOException e) {
            throw new RuntimeException("Could not serialize tags", e);
        }
    }
}
