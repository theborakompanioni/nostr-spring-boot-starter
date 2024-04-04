package org.tbk.nostr.example.relay.impl.nip50;

import com.github.pemistahl.lingua.api.IsoCode639_1;
import com.github.pemistahl.lingua.api.Language;
import com.google.protobuf.Descriptors;
import lombok.*;
import org.tbk.nostr.proto.Filter;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Nip50SearchTerm {
    private static final Descriptors.FieldDescriptor searchFieldDescriptor = Filter.getDescriptor().findFieldByNumber(Filter.SEARCH_FIELD_NUMBER);

    public static Optional<Nip50SearchTerm> from(Filter filter) {
        return Optional.of(filter)
                .filter(it -> it.hasField(searchFieldDescriptor))
                .map(Filter::getSearch)
                .filter(it -> !it.isBlank())
                .map(Nip50SearchTerm::tryParse)
                .flatMap(it -> it);
    }

    public static Optional<Nip50SearchTerm> tryParse(String input) {
        try {
            return Optional.of(parse(input));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static Nip50SearchTerm parse(String input) {
        String sanitizedInput = Options.stripOptionValues(input);
        return new Nip50SearchTerm(sanitizedInput, Options.parseFrom(input));
    }

    @NonNull
    String search;

    @NonNull
    Options options;

    public boolean hasSearchTerm() {
        return !search.isBlank();
    }

    @Value
    @Builder
    public static class Options {
        public static String stripOptionValues(String input) {
            StringTokenizer tokenizer = new StringTokenizer(input);

            StringBuilder sb = new StringBuilder();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                String[] keyValue = token.split(":");
                if (keyValue.length > 0 && keyValue.length != 2) {
                    sb.append(" ").append(keyValue[0]);
                }
            }
            return sb.toString().strip();
        }

        public static Options parseFrom(String input) {
            String lowerCaseInput = input.toLowerCase(Locale.ROOT);
            return Options.builder()
                    .includeSpam(lowerCaseInput.contains("include:spam"))
                    .language(tryParseLanguageFrom(lowerCaseInput))
                    .nsfw(!lowerCaseInput.contains("nsfw:false"))
                    .sentiment(
                            lowerCaseInput.contains("sentiment:positive") ? Integer.valueOf(1) :
                                    (lowerCaseInput.contains("sentiment:neutral") ? Integer.valueOf(0) :
                                            (lowerCaseInput.contains("sentiment:negative") ? -1 : null))
                    )
                    .build();
        }

        private static @Nullable Language tryParseLanguageFrom(String lowerCaseInput) {
            Matcher matcher = Pattern.compile("language:(\\w\\w)").matcher(lowerCaseInput);
            if (!matcher.find()) {
                return null;
            }
            try {
                IsoCode639_1 isoCode = IsoCode639_1.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
                return Language.getByIsoCode639_1(isoCode);
            } catch (Exception e) {
                return null;
            }
        }


        private static @Nullable Integer tryParseSentimentFrom(String lowerCaseInput) {
            Matcher matcher = Pattern.compile(".*sentiment:(positive|neutral|negative).*").matcher(lowerCaseInput);
            if (!matcher.find()) {
                return null;
            }
            try {
                String sentiment = matcher.group(1).toLowerCase(Locale.ROOT);
                return "positive".equals(sentiment) ? Integer.valueOf(1) : (
                        "neutral".equals(sentiment) ? Integer.valueOf(0) : (
                                "negative".contains(sentiment) ? -1 : null
                        )
                );
            } catch (Exception e) {
                return null;
            }
        }

        private final static Options DEFAULT_OPTIONS = Options.builder().build();

        public static Options defaultOptions() {
            return DEFAULT_OPTIONS;
        }

        // include:spam - turn off spam filtering, if it was enabled by default
        @Builder.Default
        boolean includeSpam = false;
        //domain:<domain> - include only events from users whose valid nip05 domain matches the domain
        //String domain;
        //language:<two-letter ISO 639-1 language code> - include only events of a specified language
        Language language;
        //sentiment:<negative/neutral/positive> - include only events of a specific sentiment
        Integer sentiment;
        //nsfw:<true/false> - include or exclude nsfw events (default: true)
        @Builder.Default
        boolean nsfw = true;

        //public Optional<String> getDomain() {
        //    return Optional.ofNullable(domain);
        //}

        public Optional<Language> getLanguage() {
            return Optional.ofNullable(language);
        }

        public Optional<Integer> getSentiment() {
            return Optional.ofNullable(sentiment);
        }
    }
}
