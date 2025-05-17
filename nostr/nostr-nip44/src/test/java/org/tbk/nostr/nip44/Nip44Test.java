package org.tbk.nostr.nip44;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.PrivateKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.persona.Persona;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MorePublicKeys;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip44Test {

    @Test
    void itShouldCreateConversationKey0() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        PrivateKey conversationKeyAlice = Nip44.getConversationKey(alice.getPrivateKey(), bob.getPublicKey());
        PrivateKey conversationKeyBob = Nip44.getConversationKey(bob.getPrivateKey(), alice.getPublicKey());

        assertThat(conversationKeyBob, is(conversationKeyAlice));
        assertThat(conversationKeyAlice.value.toHex(), is("c8c66b606288809c96efd4551f7966fff51272529cbe79306beb3fe5ab30b962"));
    }

    @Test
    void itShouldCreateConversationKey1() {
        Identity.Account random0 = MoreIdentities.random().deriveAccount(0);
        Identity.Account random1 = MoreIdentities.random().deriveAccount(0);

        PrivateKey conversationKey0 = Nip44.getConversationKey(random0.getPrivateKey(), random1.getPublicKey());
        PrivateKey conversationKey1 = Nip44.getConversationKey(random1.getPrivateKey(), random0.getPublicKey());
        assertThat(conversationKey1, is(conversationKey0));
    }

    @Test
    void itShouldEncryptAndDecrypt() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        String preimage = "GM";

        PrivateKey conversationKeyAlice = Nip44.getConversationKey(alice.getPrivateKey(), bob.getPublicKey());
        String encrypted = Nip44.encrypt(conversationKeyAlice, preimage);

        PrivateKey conversationKeyBob = Nip44.getConversationKey(bob.getPrivateKey(), alice.getPublicKey());
        String decrypted = Nip44.decrypt(conversationKeyBob, encrypted);

        assertThat(decrypted, is(preimage));
    }

    @Test
    void itShouldEncryptAndDecryptGiftWrap() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        String preimage = "GM";

        PrivateKey conversationKeyAlice = Nip44.getConversationKey(alice.getPrivateKey(), bob.getPublicKey());
        Event.Builder rumor = Nip1.createEvent(alice.getPublicKey(), preimage, Kinds.kindDirectMessage.getValue());

        Event sealed = MoreEvents.finalize(SimpleSigner.fromAccount(alice), Event.newBuilder()
                .setCreatedAt(Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()).getEpochSecond())
                .setPubkey(ByteString.fromHex(alice.getPublicKey().value.toHex()))
                .setKind(Kinds.kindSeal.getValue())
                .setContent(Nip44.encrypt(conversationKeyAlice, JsonWriter.toJson(rumor.build())))
        );

        Identity.Account ephemeralIdentity = MoreIdentities.random().deriveAccount(0);
        PrivateKey ephemeralConversationKeyAlice = Nip44.getConversationKey(ephemeralIdentity.getPrivateKey(), bob.getPublicKey());

        Event giftWrapped = MoreEvents.finalize(SimpleSigner.fromAccount(ephemeralIdentity), Event.newBuilder()
                .setCreatedAt(Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()).getEpochSecond())
                .setPubkey(ByteString.fromHex(ephemeralIdentity.getPublicKey().value.toHex()))
                .setKind(Kinds.kindSeal.getValue())
                .setContent(Nip44.encrypt(ephemeralConversationKeyAlice, JsonWriter.toJson(sealed)))
        );

        PrivateKey ephemeralConversationKeyBob = Nip44.getConversationKey(bob.getPrivateKey(), MorePublicKeys.fromEvent(giftWrapped));
        String decryptedSealed = Nip44.decrypt(ephemeralConversationKeyBob, giftWrapped.getContent());
        Event decryptedSealedEvent = JsonReader.fromJson(decryptedSealed, Event.newBuilder());

        PrivateKey conversationKeyBob = Nip44.getConversationKey(bob.getPrivateKey(), MorePublicKeys.fromEvent(decryptedSealedEvent));
        String decryptedRumor = Nip44.decrypt(conversationKeyBob, decryptedSealedEvent.getContent());
        Event decryptedRumorEvent = JsonReader.fromJson(decryptedRumor, Event.newBuilder());

        assertThat(decryptedRumorEvent.getContent(), is(preimage));
    }
}
