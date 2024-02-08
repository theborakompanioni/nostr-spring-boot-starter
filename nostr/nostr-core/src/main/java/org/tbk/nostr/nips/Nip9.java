package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/09.md">NIP-09</a>.
 */
public final class Nip9 {
    private static final int DELETION_EVENT_KIND = 5;

    private Nip9() {
        throw new UnsupportedOperationException();
    }

    public static int kind() {
        return DELETION_EVENT_KIND;
    }

    public static Event.Builder createDeletionEvent(XonlyPublicKey publicKey, List<EventId> eventId) {
        return createDeletionEvent(publicKey, null, eventId);
    }

    public static Event.Builder createDeletionEvent(XonlyPublicKey publicKey, @Nullable String reason, List<EventId> eventId) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(DELETION_EVENT_KIND)
                .addAllTags(eventId.stream().map(MoreTags::e).collect(Collectors.toSet()))
                .setContent(reason == null ? "" : reason));
    }

    public static boolean isDeletionEvent(EventOrBuilder event) {
        return event.getKind() == DELETION_EVENT_KIND;
    }

}
