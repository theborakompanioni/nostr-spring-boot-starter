package org.tbk.nostr.nip59;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nip44.Nip44;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;
import org.tbk.nostr.util.MoreTags;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

public final class Nip59 {

    private Nip59() {
        throw new UnsupportedOperationException();
    }

    public static Event unwrapGift(Event giftWrap, PrivateKey privateKey) {
        Event seal = unwrapOnce(giftWrap, privateKey);
        return unwrapOnce(seal, privateKey);
    }

    public static Event unwrapOnce(Event wrapper, PrivateKey privateKey) {
        PrivateKey wrapperKey = Nip44.getConversationKey(privateKey, MorePublicKeys.fromEvent(wrapper));

        String decryptedContent = Nip44.decrypt(wrapperKey, wrapper.getContent());
        return JsonReader.fromJson(decryptedContent, Event.newBuilder());
    }

    public static Event giftWrap(Event rumor, PrivateKey privateKey, XonlyPublicKey recipientPublicKey) {
        Event seal = seal(rumor, privateKey, recipientPublicKey);
        return wrap(seal, recipientPublicKey);
    }

    public static Event seal(Event rumor, PrivateKey privateKey, XonlyPublicKey recipientPublicKey) {
        if (rumor.hasField(Event.getDescriptor().findFieldByNumber(Event.SIG_FIELD_NUMBER))) {
            throw new IllegalArgumentException("Given rumor event must not be signed.");
        }

        PrivateKey conversationKey = Nip44.getConversationKey(privateKey, recipientPublicKey);

        return MoreEvents.finalize(SimpleSigner.fromPrivateKey(privateKey), Event.newBuilder()
                .setCreatedAt(MoreRandom.randomInstant().getEpochSecond())
                .setPubkey(ByteString.fromHex(privateKey.publicKey().xOnly().value.toHex()))
                .setKind(Kinds.kindSeal.getValue())
                .setContent(Nip44.encrypt(conversationKey, JsonWriter.toJson(rumor)))
        );
    }

    public static Event wrap(Event seal, XonlyPublicKey recipientPublicKey) {
        Identity.Account ephemeralIdentity = MoreIdentities.random().deriveAccount(0);
        PrivateKey ephemeralKey = Nip44.getConversationKey(ephemeralIdentity.getPrivateKey(), recipientPublicKey);

        return MoreEvents.finalize(SimpleSigner.fromAccount(ephemeralIdentity), Event.newBuilder()
                .setCreatedAt(MoreRandom.randomInstant().getEpochSecond())
                .setPubkey(ByteString.fromHex(ephemeralIdentity.getPublicKey().value.toHex()))
                .setKind(Kinds.kindGiftWrap.getValue())
                .setContent(Nip44.encrypt(ephemeralKey, JsonWriter.toJson(seal)))
                .addTags(MoreTags.p(recipientPublicKey))
        );
    }

    private static final class MoreRandom {
        private static final SecureRandom RANDOM = new SecureRandom();
        private static final Duration TWO_DAYS = Duration.ofDays(2);

        private MoreRandom() {
            throw new UnsupportedOperationException();
        }

        static Instant randomInstant() {
            return randomInstant(TWO_DAYS);
        }

        static Instant randomInstant(Duration duration) {
            return Instant.now().minusSeconds(Math.round(RANDOM.nextFloat() * duration.toSeconds()));
        }
    }
}
