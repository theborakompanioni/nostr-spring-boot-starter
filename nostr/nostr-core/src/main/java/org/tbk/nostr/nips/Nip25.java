package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/25.md">NIP-25</a>.
 */
public final class Nip25 {

    private Nip25() {
        throw new UnsupportedOperationException();
    }

    public static Event.Builder like(XonlyPublicKey publicKey, Event event) {
        return reaction(publicKey, event, "+");
    }

    public static Event.Builder dislike(XonlyPublicKey publicKey, Event event) {
        return reaction(publicKey, event, "-");
    }

    public static Event.Builder website(XonlyPublicKey publicKey, URI websiteUrl, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindReactionToWebsite.getValue())
                .addTags(MoreTags.r(websiteUrl.normalize()))
                .setContent(content));
    }

    public static Event.Builder emoji(XonlyPublicKey publicKey, Event event, URI imageUrl) {
        return reaction(publicKey, event, Nip30.emoji("emoji", imageUrl));
    }

    public static Event.Builder reaction(XonlyPublicKey publicKey, Event event, Nip30.Emoji emoji) {
        return reaction(publicKey, event, emoji.placeholder()).addTags(emoji.tag());
    }

    public static Event.Builder reaction(XonlyPublicKey publicKey, Event event, String content) {
        Event.Builder builder = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindReaction.getValue())
                .addAllTags(MoreTags.findByName(event, IndexedTag.e).stream()
                        .filter(it -> it.getValuesCount() >= 1)
                        .toList())
                .addAllTags(MoreTags.findByName(event, IndexedTag.p).stream()
                        .filter(it -> it.getValuesCount() >= 1)
                        .toList())
                .addTags(MoreTags.e(event))
                .addTags(MoreTags.p(event))
                .addTags(MoreTags.k(event))
                .setContent(content));

        boolean includeReplaceableTags = Nip1.isReplaceableEvent(event) || Nip1.isAddressableEvent(event);
        if (includeReplaceableTags) {
            builder.addAllTags(MoreTags.findByName(event, IndexedTag.a).stream()
                            .filter(it -> it.getValuesCount() >= 1)
                            .toList())
                    .addTags(MoreTags.a(event));
        }

        return builder;
    }
}
