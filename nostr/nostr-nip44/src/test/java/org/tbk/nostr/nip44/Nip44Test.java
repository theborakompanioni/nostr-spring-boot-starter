package org.tbk.nostr.nip44;

import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.PrivateKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.persona.Persona;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.proto.json.JsonWriter;
import org.tbk.nostr.util.MoreEvents;

import java.time.Duration;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip44Test {

    @Test
    void itShouldEncryptAndDecrypt() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        PrivateKey conversationKey = Nip44.getConversationKey(alice.getPrivateKey(), bob.getPublicKey());
        Event.Builder unsignedKind14 = Nip1.createEvent(alice.getPublicKey(), "GM", Kinds.kindDirectMessage.getValue());

        Event sealed = MoreEvents.finalize(SimpleSigner.fromAccount(alice), Event.newBuilder()
                .setCreatedAt(Instant.now().minusSeconds(Duration.ofDays(1).toSeconds()).getEpochSecond())
                .setPubkey(ByteString.fromHex(alice.getPublicKey().value.toHex()))
                .setKind(Kinds.kindSeal.getValue())
                .setContent(Nip44.encrypt(conversationKey, JsonWriter.toJson(unsignedKind14.build())))
        );

        String decryptedPayload = Nip44.decrypt(conversationKey, sealed.getContent());
        Event decryptedEvent = JsonReader.fromJson(decryptedPayload, Event.newBuilder());

        assertThat(decryptedEvent.getContent(), is("GM"));
    }
}
