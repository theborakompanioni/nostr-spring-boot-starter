package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
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

    public static boolean isDeletionEvent(EventOrBuilder event) {
        return event.getKind() == DELETION_EVENT_KIND;
    }

    public static Event.Builder createDeletionEventForEvent(XonlyPublicKey publicKey, Event event) {
        if (Nip1.isReplaceableEvent(event)) {
            return createDeletionEventForReplaceableEvent(publicKey, event.getKind());
        } else if (Nip1.isParameterizedReplaceableEvent(event)) {
            String dTagValue = MoreTags.findByNameSingle(event, "d")
                    .map(it -> it.getValues(0))
                    .orElseThrow(() -> new IllegalStateException("Expected an `d` tag"));
            return createDeletionEventForParameterizedReplaceableEvent(publicKey, event.getKind(), dTagValue);
        }

        return createDeletionEvent(publicKey, null, Collections.singletonList(EventId.of(event.getId().toByteArray())));
    }

    public static Event.Builder createDeletionEvent(XonlyPublicKey publicKey, Collection<EventId> eventId) {
        return createDeletionEvent(publicKey, null, eventId);
    }

    public static Event.Builder createDeletionEvent(XonlyPublicKey publicKey, @Nullable String reason, Collection<EventId> eventId) {
        return createDeletionEventInternal(publicKey, reason, eventId.stream()
                .map(MoreTags::e)
                .collect(Collectors.toSet())
        );
    }

    public static Event.Builder createDeletionEventForReplaceableEvent(XonlyPublicKey publicKey, int kind) {
        return createDeletionEventForReplaceableEvent(publicKey, null, kind);
    }

    public static Event.Builder createDeletionEventForReplaceableEvent(XonlyPublicKey publicKey, @Nullable String reason, int kind) {
        return createDeletionEventInternal(publicKey, reason, Collections.singletonList(MoreTags.a(kind, publicKey)));
    }

    public static Event.Builder createDeletionEventForParameterizedReplaceableEvent(XonlyPublicKey publicKey, int kind, @Nullable String dTagValue) {
        return createDeletionEventForParameterizedReplaceableEvent(publicKey, null, kind, dTagValue);
    }

    public static Event.Builder createDeletionEventForParameterizedReplaceableEvent(XonlyPublicKey publicKey, @Nullable String reason, int kind, @Nullable String dTagValue) {
        return createDeletionEventInternal(publicKey, reason, Collections.singletonList(MoreTags.a(kind, publicKey, dTagValue)));
    }

    private static Event.Builder createDeletionEventInternal(XonlyPublicKey publicKey,
                                                             @Nullable String reason,
                                                             Collection<TagValue> tags) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(DELETION_EVENT_KIND)
                .addAllTags(tags)
                .setContent(reason == null ? "" : reason));
    }
}
