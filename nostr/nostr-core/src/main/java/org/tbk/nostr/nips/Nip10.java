package org.tbk.nostr.nips;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/10.md">NIP-10</a>.
 */
public final class Nip10 {
    private Nip10() {
        throw new UnsupportedOperationException();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Marker {
        REPLY("reply"),
        ROOT("root"),
        MENTION("mention");

        final String value;

        public TagValue tag(EventId eventId) {
            return tag(eventId, null);
        }

        public TagValue tag(EventId eventId, @Nullable RelayUri recommendedRelay) {
            if (recommendedRelay == null) {
                return MoreTags.e(eventId.toHex(), "", this.value);
            } else {
                return MoreTags.e(eventId.toHex(), recommendedRelay.getUri().toString(), this.value);
            }
        }
    }
}
