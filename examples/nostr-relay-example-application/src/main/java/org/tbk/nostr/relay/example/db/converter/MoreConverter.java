package org.tbk.nostr.relay.example.db.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;

public final class MoreConverter {

    private MoreConverter() {
        throw new UnsupportedOperationException();
    }

    @Converter
    public static final class InstantToMilliSecondsConverter implements AttributeConverter<Instant, Long> {

        @Override
        public Long convertToDatabaseColumn(Instant attribute) {
            return attribute == null ? null : attribute.toEpochMilli();
        }

        @Override
        public Instant convertToEntityAttribute(Long dbData) {
            return dbData == null ? null : Instant.ofEpochMilli(dbData);
        }
    }

    @Converter
    public static final class InstantToSecondsConverter implements AttributeConverter<Instant, Long> {

        @Override
        public Long convertToDatabaseColumn(Instant attribute) {
            return attribute == null ? null : attribute.getEpochSecond();
        }

        @Override
        public Instant convertToEntityAttribute(Long dbData) {
            return dbData == null ? null : Instant.ofEpochSecond(dbData);
        }
    }
}
