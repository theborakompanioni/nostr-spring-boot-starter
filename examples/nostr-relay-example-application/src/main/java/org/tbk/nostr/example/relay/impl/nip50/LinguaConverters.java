package org.tbk.nostr.example.relay.impl.nip50;

import com.github.pemistahl.lingua.api.IsoCode639_1;
import com.github.pemistahl.lingua.api.Language;
import com.google.common.collect.ImmutableMap;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class LinguaConverters {

    private LinguaConverters() {
        throw new UnsupportedOperationException();
    }

    @Converter
    public static final class LanguageToIso6391Converter implements AttributeConverter<Language, String> {

        @Override
        public String convertToDatabaseColumn(Language attribute) {
            return attribute == null ? null : attribute.getIsoCode639_1().name();
        }

        @Override
        public Language convertToEntityAttribute(String dbData) {
            return dbData == null ? null : Optional.of(dbData)
                    .map(IsoCode639_1::valueOf)
                    .filter(it -> it != IsoCode639_1.NONE)
                    .map(Language::getByIsoCode639_1)
                    .orElse(null);
        }
    }

    @Converter
    public static final class LanguageToPostgresTsCfgnameConverter implements AttributeConverter<Language, String> {

        // Postgres default languages (last checked postgres v16.0 on 2024-04-03)
        // `SELECT cfgname FROM pg_ts_config;`
        private static final Map<Language, String> mapping = ImmutableMap.<Language, String>builder()
                .put(Language.UNKNOWN, "simple")
                .put(Language.ARABIC, "arabic")
                .put(Language.ARMENIAN, "armenian")
                .put(Language.BASQUE, "basque")
                .put(Language.CATALAN, "catalan")
                .put(Language.DANISH, "danish")
                .put(Language.DUTCH, "dutch")
                .put(Language.ENGLISH, "english")
                .put(Language.FINNISH, "finnish")
                .put(Language.FRENCH, "french")
                .put(Language.GERMAN, "german")
                .put(Language.GREEK, "greek")
                .put(Language.HINDI, "hindi")
                .put(Language.HUNGARIAN, "hungarian")
                .put(Language.INDONESIAN, "indonesian")
                .put(Language.IRISH, "irish")
                .put(Language.ITALIAN, "italian")
                .put(Language.LITHUANIAN, "lithuanian")
                // Not supported: .put(Language.NEPALI, "nepali")
                // Not supported: .put(Language.NORWEGIAN, "norwegian")
                .put(Language.PORTUGUESE, "portuguese")
                .put(Language.ROMANIAN, "romanian")
                .put(Language.RUSSIAN, "russian")
                .put(Language.SERBIAN, "serbian")
                .put(Language.SPANISH, "spanish")
                .put(Language.SWEDISH, "swedish")
                .put(Language.TAMIL, "tamil")
                .put(Language.TURKISH, "turkish")
                // Not supported: .put(Language.YIDDISH, "yiddish")
                .build();

        private static final Map<String, Language> reverseMapping = mapping.entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        @Override
        public String convertToDatabaseColumn(Language attribute) {
            return attribute == null ? null : mapping.getOrDefault(attribute, mapping.getOrDefault(Language.UNKNOWN, "simple"));
        }

        @Override
        public Language convertToEntityAttribute(String dbData) {
            return dbData == null ? null : reverseMapping.getOrDefault(dbData, Language.UNKNOWN);
        }
    }
}
