package org.tbk.nostr.nips;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

/**
 * See <a href="https://github.com/nostr-protocol/nips/blob/master/98.md">NIP-98</a>.
 */
public final class Nip98 {

    private Nip98() {
        throw new UnsupportedOperationException();
    }

    public static Event.Builder createAuthEvent(XonlyPublicKey publicKey, URI url, String method) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(Kinds.kindHTTPAuth.getValue())
                .addTags(MoreTags.u(url))
                .addTags(MoreTags.named("method", method))
                .setContent(""));
    }

    public static Event.Builder createAuthEventWithBody(XonlyPublicKey publicKey,
                                                        URI url,
                                                        String method,
                                                        String body) {
        byte[] payloadHash = Crypto.sha256(body.getBytes(StandardCharsets.UTF_8));
        return createAuthEventWithPayloadHash(publicKey, url, method, payloadHash);
    }

    public static Event.Builder createAuthEventWithPayloadHash(XonlyPublicKey publicKey,
                                                               URI url,
                                                               String method,
                                                               byte[] bodyHash) {
        return createAuthEvent(publicKey, url, method)
                .addTags(MoreTags.named("payload", HexFormat.of().formatHex(bodyHash)));
    }

    public static TagValue payloadTagFromBody(String body) {
        byte[] bodyHash = Crypto.sha256(body.getBytes(StandardCharsets.UTF_8));
        return payloadTagFromBodyHash(bodyHash);
    }

    public static TagValue payloadTagFromBodyHash(byte[] bodyHash) {
        return MoreTags.named("payload", HexFormat.of().formatHex(bodyHash));
    }
}
