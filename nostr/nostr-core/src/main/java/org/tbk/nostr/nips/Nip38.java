package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.time.Instant;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/38.md">NIP-38</a>.
 */
public final class Nip38 {

    private Nip38() {
        throw new UnsupportedOperationException();
    }

    @Getter
    @RequiredArgsConstructor
    public enum StatusType {
        GENERAL("general"),
        MUSIC("music");

        private final String value;
    }

    public static Event.Builder clear(XonlyPublicKey publicKey, StatusType status) {
        return clear(publicKey, status.getValue());
    }

    public static Event.Builder clear(XonlyPublicKey publicKey, String status) {
        return status(publicKey, status, "");
    }

    public static Event.Builder music(XonlyPublicKey publicKey, URI musicUri, String description, Nip40.Expiration expiration) {
        return status(publicKey, StatusType.MUSIC, description, expiration).addTags(MoreTags.r(musicUri));
    }

    public static Event.Builder general(XonlyPublicKey publicKey, Nip30.Emoji emoji) {
        return general(publicKey, emoji.placeholder()).addTags(emoji.tag());
    }

    public static Event.Builder general(XonlyPublicKey publicKey, Nip30.Emoji emoji, Nip40.Expiration expiration) {
        return general(publicKey, emoji).addTags(expiration.tag());
    }

    public static Event.Builder general(XonlyPublicKey publicKey, String content) {
        return status(publicKey, StatusType.GENERAL, content);
    }

    public static Event.Builder general(XonlyPublicKey publicKey, String content, Nip40.Expiration expiration) {
        return general(publicKey, content).addTags(expiration.tag());
    }

    public static Event.Builder status(XonlyPublicKey publicKey, String status, Nip30.Emoji emoji) {
        return status(publicKey, status, emoji.placeholder()).addTags(emoji.tag());
    }

    public static Event.Builder status(XonlyPublicKey publicKey, String status, Nip30.Emoji emoji, Nip40.Expiration expiration) {
        return status(publicKey, status, emoji).addTags(expiration.tag());
    }

    public static Event.Builder status(XonlyPublicKey publicKey, StatusType status, String content) {
        return status(publicKey, status.getValue(), content);
    }
    public static Event.Builder status(XonlyPublicKey publicKey, StatusType status, String content, Nip40.Expiration expiration) {
        return status(publicKey, status, content).addTags(expiration.tag());
    }

    public static Event.Builder status(XonlyPublicKey publicKey, String status, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindUserStatuses.getValue())
                .addTags(MoreTags.d(status))
                .setContent(content));
    }

    public static Event.Builder status(XonlyPublicKey publicKey, String status, String content, Nip40.Expiration expiration) {
        return status(publicKey, status, content).addTags(expiration.tag());
    }
}
