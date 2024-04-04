package org.tbk.nostr.example.relay.domain.event;


import com.github.pemistahl.lingua.api.Language;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.jmolecules.ddd.types.Entity;
import org.jmolecules.ddd.types.Identifier;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.tbk.nostr.example.relay.db.converter.MoreConverter;
import org.tbk.nostr.example.relay.impl.nip50.LinguaConverters;
import org.tbk.nostr.proto.Event;

import static java.util.Objects.requireNonNull;

@Getter
@Table(name = "event_nip50_meta_info")
public class EventNip50MetaInfoEntity implements Entity<EventEntity, EventNip50MetaInfoEntity.EventNip50MetaInfoEntityId> {

    @NonNull
    private final EventNip50MetaInfoEntity.EventNip50MetaInfoEntityId id;

    @NonNull
    @Column(name = "language_iso639_1", nullable = false, updatable = false)
    @Convert(converter = LinguaConverters.LanguageToIso6391Converter.class)
    private final Language language;

    @NonNull
    @Column(name = "postgres_ts_config_cfgname", nullable = false, updatable = false)
    @Convert(converter = LinguaConverters.LanguageToPostgresTsCfgnameConverter.class)
    private final Language postgresLanguage;

    @NonNull
    @Column(name = "searchable_content", nullable = false, updatable = false)
    private final String searchableContent;

    // needed in order to stay compatible with sqlite FTS5 query syntax (a column named same as the table)
    @ReadOnlyProperty
    @Column(name = "event_nip50_meta_info", nullable = false, updatable = false, insertable = false)
    private Object eventNip50MetaInfoEntity;

    /**
     * Creates a new {@link EventNip50MetaInfoEntity} for the given {@link Event}.
     *
     * @param eventId must not be {@literal null}.
     */
    public EventNip50MetaInfoEntity(EventEntity.EventEntityId eventId,
                                    Language language,
                                    String content) {
        this.id = EventNip50MetaInfoEntityId.from(eventId);
        this.language = requireNonNull(language);
        this.postgresLanguage = requireNonNull(language);
        this.searchableContent = requireNonNull(content);
    }

    public String getEventId() {
        return this.id.getEventId();
    }

    @Value(staticConstructor = "of")
    public static class EventNip50MetaInfoEntityId implements Identifier {
        static EventNip50MetaInfoEntityId from(EventEntity.EventEntityId eventId) {
            return EventNip50MetaInfoEntityId.of(eventId.getId());
        }

        @NonNull
        @Column(name = "event_id", nullable = false, updatable = false)
        String eventId;
    }
}
