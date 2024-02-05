package org.tbk.nostr.util;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonWriter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;

public final class MoreEvents {

    private MoreEvents() {
        throw new UnsupportedOperationException();
    }


    public static Event.Builder withEventId(Event.Builder event) {
        return event.setId(ByteString.copyFrom(eventId(event)));
    }

    public static byte[] eventId(Event.Builder event) {
        return Crypto.sha256(JsonWriter.toJsonForSigning(event).getBytes(StandardCharsets.UTF_8));
    }

    public static Event finalize(Signer signer, Event.Builder event) {
        if (event.getCreatedAt() == 0L) {
            event.setCreatedAt(Instant.now().getEpochSecond());
        }
        event.setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()));
        event.setId(ByteString.copyFrom(eventId(event)));
        return signer.sign(event).build();
    }

    public static boolean isValid(Event event) {
        try {
            verify(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Event verify(Event event) {
        ByteVector32 eventId = ByteVector32.fromValidHex(EventId.of(event.getId().toByteArray()).toHex());
        XonlyPublicKey publicKey = new XonlyPublicKey(ByteVector32.fromValidHex(HexFormat.of().formatHex(event.getPubkey().toByteArray())));

        if (!publicKey.getPublicKey().isValid()) {
            throw new IllegalArgumentException("Invalid public key");
        }
        if (event.getKind() < 0 || event.getKind() > 65_535) {
            throw new IllegalArgumentException("Invalid kind");
        }
        if (event.getCreatedAt() < 0L) {
            throw new IllegalArgumentException("Invalid created timestamp");
        }

        ByteVector signature = ByteVector.fromHex(HexFormat.of().formatHex(event.getSig().toByteArray()));
        boolean isValidSignature = Crypto.verifySignatureSchnorr(eventId, signature, publicKey);
        if (!isValidSignature) {
            throw new IllegalArgumentException("Invalid signature");

        }
        return event;
    }

    public static Event.Builder createTextMessage(Signer signer, String content) {
        return createTextMessage(signer.getPublicKey(), content);
    }

    public static Event.Builder createTextMessage(XonlyPublicKey publicKey, String content) {
        return MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(publicKey.value.toHex()))
                .setKind(1)
                .setContent(content));
    }

    public static Event createFinalizedTextMessage(Signer signer, String content) {
        return finalize(signer, Event.newBuilder()
                .setCreatedAt(Instant.now().getEpochSecond())
                .setPubkey(ByteString.fromHex(signer.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent(content));
    }
}
