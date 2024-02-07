package org.tbk.nostr.util;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.ByteVector;
import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.Crypto;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventOrBuilder;
import org.tbk.nostr.proto.json.JsonWriter;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;

public final class MoreEvents {

    private MoreEvents() {
        throw new UnsupportedOperationException();
    }


    public static Event.Builder withEventId(Event.Builder event) {
        return event.setId(ByteString.copyFrom(eventId(event)));
    }

    public static byte[] eventId(EventOrBuilder event) {
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

    public static Event verify(Event event) throws IllegalArgumentException {

        if (event.getKind() < 0 || event.getKind() > 65_535) {
            throw new IllegalArgumentException("Invalid kind.");
        }
        if (event.getCreatedAt() < 0L) {
            throw new IllegalArgumentException("Invalid created timestamp.");
        }

        XonlyPublicKey publicKey = new XonlyPublicKey(ByteVector32.fromValidHex(HexFormat.of().formatHex(event.getPubkey().toByteArray())));
        if (!publicKey.getPublicKey().isValid()) {
            throw new IllegalArgumentException("Invalid public key.");
        }

        byte[] calculatedEventId = eventId(event);
        if (!Arrays.equals(calculatedEventId, event.getId().toByteArray())) {
            throw new IllegalArgumentException("Invalid id.");
        }

        ByteVector signature = ByteVector.fromHex(HexFormat.of().formatHex(event.getSig().toByteArray()));
        boolean isValidSignature = Crypto.verifySignatureSchnorr(new ByteVector32(calculatedEventId), signature, publicKey);
        if (!isValidSignature) {
            throw new IllegalArgumentException("Invalid signature.");

        }
        return event;
    }

    public static Event createFinalizedTextNote(Signer signer, String content) {
        return finalize(signer, Nip1.createTextNote(signer.getPublicKey(), content));
    }

    public static Event createFinalizedMetadata(Signer signer, Metadata metadata) {
        return finalize(signer, Nip1.createMetadata(signer.getPublicKey(), metadata));
    }
}
