package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.EventUri;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;
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

    private Nip9() {
        throw new UnsupportedOperationException();
    }

    public static boolean isDeletionEvent(EventOrBuilder event) {
        return event.getKind() == Kinds.kindDeletion.getValue();
    }

    public static Event.Builder createDeletionEventForEvent(Event event) {
        XonlyPublicKey publicKey = MorePublicKeys.fromEvent(event);
        if (Nip1.isReplaceableEvent(event)) {
            return createDeletionEventForReplaceableEvent(publicKey, event.getKind());
        } else if (Nip1.isAddressableEvent(event)) {
            String dTagValue = MoreTags.findByNameSingle(event, IndexedTag.d.name())
                    .map(it -> it.getValues(0))
                    .orElseThrow(() -> new IllegalStateException("Expected an `%s` tag".formatted(IndexedTag.d.name())));
            return createDeletionEventForAddressableEvent(publicKey, event.getKind(), dTagValue);
        }

        return createDeletionEvent(publicKey, null, Collections.singletonList(EventId.of(event.getId().toByteArray())));
    }

    public static Event.Builder createDeletionEventForEvent(EventUri eventUri) {
        return createDeletionEventForEvent(eventUri, null);
    }

    public static Event.Builder createDeletionEventForEvent(EventUri eventUri, @Nullable String reason) {
        return createDeletionEventForAddressableEvent(MorePublicKeys.fromHex(eventUri.getPublicKeyHex()), reason, eventUri.getKind().getValue(), eventUri.getIdentifier().orElse(null));
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

    public static Event.Builder createDeletionEventForAddressableEvent(XonlyPublicKey publicKey, int kind, @Nullable String dTagValue) {
        return createDeletionEventForAddressableEvent(publicKey, null, kind, dTagValue);
    }

    public static Event.Builder createDeletionEventForAddressableEvent(XonlyPublicKey publicKey, @Nullable String reason, int kind, @Nullable String dTagValue) {
        return createDeletionEventInternal(publicKey, reason, Collections.singletonList(MoreTags.a(kind, publicKey, dTagValue)));
    }

    private static Event.Builder createDeletionEventInternal(XonlyPublicKey publicKey,
                                                             @Nullable String reason,
                                                             Collection<TagValue> tags) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindDeletion.getValue())
                .addAllTags(tags)
                .setContent(reason == null ? "" : reason));
    }
}
