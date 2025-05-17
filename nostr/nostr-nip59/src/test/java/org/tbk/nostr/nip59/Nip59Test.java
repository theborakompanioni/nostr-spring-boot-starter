package org.tbk.nostr.nip59;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.persona.Persona;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip59Test {

    @Test
    void itShouldCreateGiftWrap() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        Event rumor = Nip1.createEvent(alice.getPublicKey(), "GM", Kinds.kindDirectMessage.getValue()).build();
        Event giftWrap = Nip59.giftWrap(rumor, alice.getPrivateKey(), bob.getPublicKey());

        Event unwrapped = Nip59.unwrapGift(giftWrap, bob.getPrivateKey());
        assertThat(unwrapped, is(rumor));
    }

    @Test
    void itShouldVerifyGiftAndSeal() {
        Identity.Account alice = Persona.alice().deriveAccount(0);
        Identity.Account bob = Persona.bob().deriveAccount(0);

        Event rumor = Nip1.createEvent(alice.getPublicKey(), "GM", Kinds.kindDirectMessage.getValue()).build();
        Event giftWrap = Nip59.giftWrap(rumor, alice.getPrivateKey(), bob.getPublicKey());

        assertThat(giftWrap.getKind(), is(Kinds.kindGiftWrap.getValue()));
        TagValue pTag = MoreTags.findByNameSingle(giftWrap, IndexedTag.p).orElseThrow();
        assertThat(pTag.getValues(0), is(bob.getPublicKey().value.toHex()));

        Event seal = Nip59.unwrapOnce(giftWrap, bob.getPrivateKey());
        assertThat(seal.getKind(), is(Kinds.kindSeal.getValue()));

        Event unwrapped = Nip59.unwrapOnce(seal, bob.getPrivateKey());
        assertThat(unwrapped, is(rumor));
    }

    @Test
    void itShouldNotBeAbleToSealSignedEvent() {
        Identity.Account alice = Persona.alice().deriveAccount(0);

        Event signed = MoreEvents.finalize(SimpleSigner.fromAccount(alice),
                Nip1.createTextNote(alice.getPublicKey(), "GM")
        );

        IllegalArgumentException iae = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Nip59.seal(signed, alice.getPrivateKey(), alice.getPublicKey());
        });

        assertThat(iae.getMessage(), is("Given rumor event must not be signed."));
    }
}
