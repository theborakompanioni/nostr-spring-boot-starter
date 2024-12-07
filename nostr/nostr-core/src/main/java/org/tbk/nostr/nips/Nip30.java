package org.tbk.nostr.nips;

import com.google.common.base.CharMatcher;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/30.md">NIP-30</a>.
 */
public final class Nip30 {

    private static final CharMatcher shortcodeMatcher = CharMatcher.inRange('a', 'z')
            .or(CharMatcher.inRange('A', 'Z'))
            .or(CharMatcher.inRange('0', '9'))
            .or(CharMatcher.is('_'));

    private Nip30() {
        throw new UnsupportedOperationException();
    }

    public static boolean isValidShortcode(String shortcode) {
        return !shortcode.isEmpty() && shortcodeMatcher.matchesAllOf(shortcode);
    }

    public static Emoji emoji(String shortcode, URI imageUrl) {
        return Emoji.of(shortcode, imageUrl);
    }

    public static TagValue emojiTag(String shortcode, URI imageUrl) {
        return emoji(shortcode, imageUrl).tag();
    }

    public static String placeholder(String shortcode) {
        if (!isValidShortcode(shortcode)) {
            throw new IllegalArgumentException("Illegal characters in shortcode.");
        }
        return ":%s:".formatted(shortcode);
    }

    public static final class Emoji {
        public static Emoji of(String shortcode, URI imageUrl) {
            return new Emoji(shortcode, imageUrl);
        }

        private final TagValue tag;

        private Emoji(String shortcode, URI imageUrl) {
            if (!isValidShortcode(shortcode)) {
                throw new IllegalArgumentException("Illegal characters in shortcode.");
            }
            this.tag = MoreTags.named("emoji", shortcode, imageUrl.toString());
        }

        public TagValue tag() {
            return tag;
        }

        public String placeholder() {
            return Nip30.placeholder(tag.getValues(0));
        }
    }
}
