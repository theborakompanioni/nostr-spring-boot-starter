package org.tbk.nostr.nips;

import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/70.md">NIP-70</a>.
 */
public final class Nip70 {
    private static final TagValue protectedTag = MoreTags.named("-");

    private Nip70() {
        throw new UnsupportedOperationException();
    }

    public static boolean isProtectedEvent(EventOrBuilder event) {
        return MoreTags.findByNameFirst(event, protectedTag.getName()).isPresent();
    }

    public static TagValue protectedTag() {
        return protectedTag;
    }

    public static Event.Builder protect(Event.Builder event) {
        return isProtectedEvent(event) ? event : event.addTags(protectedTag);
    }
}
