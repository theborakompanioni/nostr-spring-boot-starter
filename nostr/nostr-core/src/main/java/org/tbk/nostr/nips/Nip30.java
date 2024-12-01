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

    public static TagValue emoji(String shortcode, URI imageUrl) {
        if (!isValidShortcode(shortcode)) {
            throw new IllegalArgumentException("Illegal characters in shortcode.");
        }
        return MoreTags.named("emoji", shortcode, imageUrl.toString());
    }
}
