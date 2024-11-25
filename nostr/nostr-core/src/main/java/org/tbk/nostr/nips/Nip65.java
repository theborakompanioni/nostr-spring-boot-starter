package org.tbk.nostr.nips;

import com.google.common.collect.Sets;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.*;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/65.md">NIP-65</a>.
 */
public final class Nip65 {
    private Nip65() {
        throw new UnsupportedOperationException();
    }

    @Getter
    @RequiredArgsConstructor
    public enum Marker {
        READ("read"),
        WRITE("write");

        private final String value;
    }

    @Value
    @Builder
    public static class ReadWriteRelays {
        @Singular
        Set<RelayUri> readRelays;
        @Singular
        Set<RelayUri> writeRelays;

        public Set<RelayUri> getReadWriteRelays() {
            return Sets.intersection(readRelays, writeRelays);
        }
    }

    public static boolean isRelayListEvent(EventOrBuilder event) {
        return event.getKind() == Kinds.kindRelayListMetadata.getValue();
    }

    public static TagValue r(RelayUri relay) {
        return MoreTags.named(IndexedTag.r.name(), relay.getUri().toString());
    }

    public static TagValue r(RelayUri relay, Marker marker) {
        return MoreTags.named(IndexedTag.r.name(), relay.getUri().toString(), marker.getValue());
    }

    public static Event.Builder createRelayListEvent(XonlyPublicKey publicKey, ReadWriteRelays relays) {
        Set<RelayUri> readWriteRelays = relays.getReadWriteRelays();
        Set<RelayUri> readRelays = Sets.difference(relays.getReadRelays(), readWriteRelays);
        Set<RelayUri> writeRelays = Sets.difference(relays.getWriteRelays(), readWriteRelays);

        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindRelayListMetadata.getValue())
                .addAllTags(readWriteRelays.stream()
                        .map(Nip65::r)
                        .toList())
                .addAllTags(readRelays.stream()
                        .map(it -> r(it, Marker.READ))
                        .toList())
                .addAllTags(writeRelays.stream()
                        .map(it -> r(it, Marker.WRITE))
                        .toList())
                .setContent(""));
    }

    public static Event.Builder createRelayListEvent(XonlyPublicKey publicKey, Set<RelayUri> readWriteRelays) {
        return createRelayListEvent(publicKey, ReadWriteRelays.builder()
                .readRelays(readWriteRelays)
                .writeRelays(readWriteRelays)
                .build());
    }

    public static ReadWriteRelays findRelays(Event event) {
        if (!isRelayListEvent(event)) {
            throw new IllegalArgumentException("Unexpected event. Expected kind %d, got: %d.".formatted(Kinds.kindRelayListMetadata.getValue(), event.getKind()));
        }

        List<TagValue> rTags = MoreTags.findByName(event, IndexedTag.r);

        List<RelayUri> readRelays = rTags.stream()
                .filter(it -> it.getValuesCount() == 1 || (it.getValuesCount() >= 1 && Marker.READ.value.equals(it.getValues(1))))
                .map(it -> RelayUri.tryParse(it.getValues(0)))
                .flatMap(Optional::stream)
                .toList();

        List<RelayUri> writeRelays = rTags.stream()
                .filter(it -> it.getValuesCount() == 1 || (it.getValuesCount() >= 1 && Marker.WRITE.value.equals(it.getValues(1))))
                .map(it -> RelayUri.tryParse(it.getValues(0)))
                .flatMap(Optional::stream)
                .toList();

        return ReadWriteRelays.builder()
                .readRelays(readRelays)
                .writeRelays(writeRelays)
                .build();
    }
}
