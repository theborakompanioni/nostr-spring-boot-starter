package org.tbk.nostr.nips;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test vectors taken from <a href="https://github.com/nostr-protocol/nips/blob/master/19.md#examples">NIP-19 examples</a>.
 */
class Nip19Test {

    @Test
    void itShouldConvertNpubSuccessfully() {
        // decode0
        XonlyPublicKey publicKey0 = Nip19.fromNpub("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg");
        assertThat(publicKey0.value.toHex(), is("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));

        // encode0
        String npub0 = Nip19.toNpub(MorePublicKeys.fromHex("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));
        assertThat(npub0, is("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"));

        // decode1
        XonlyPublicKey publicKey1 = Nip19.fromNpub("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6");
        assertThat(publicKey1.value.toHex(), is("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"));

        // encode1
        String npub1 = Nip19.toNpub(MorePublicKeys.fromHex("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"));
        assertThat(npub1, is("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6"));

        // odell
        String odellNpub = "npub1qny3tkh0acurzla8x3zy4nhrjz5zd8l9sy9jys09umwng00manysew95gx";
        String odellPubkey = "04c915daefee38317fa734444acee390a8269fe5810b2241e5e6dd343dfbecc9";

        assertThat(Nip19.fromNpub(odellNpub).value.toHex(), is(odellPubkey));
        assertThat(Nip19.toNpub(MorePublicKeys.fromHex(odellPubkey)), is(odellNpub));
    }

    @Test
    void itShouldDecodeNpubFailure() {
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> Nip19.fromNpub("npub10"));
        assertThat(e0.getMessage(), is("Error while decoding bech32"));
        assertThat(e0.getCause().getMessage(), startsWith("invalid checksum for npub1"));

        String invalidPublicKeyNpub = Nip19.toNpub(MorePublicKeys.fromHex("00".repeat(32)));
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class, () -> Nip19.fromNpub(invalidPublicKeyNpub));
        assertThat(e1.getMessage(), is("Error while decoding bech32"));
        assertThat(e1.getCause().getMessage(), is("Invalid public key value"));
    }

    @Test
    void itShouldConvertNsecSuccessfully() {
        // decode
        PrivateKey privateKey0 = Nip19.fromNsec("nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5");
        assertThat(privateKey0.value.toHex(), is("67dea2ed018072d675f5415ecfaed7d2597555e202d85b3d65ea4e58d2d92ffa"));

        // encode
        String nsec0 = Nip19.toNsec(new PrivateKey(HexFormat.of().parseHex("67dea2ed018072d675f5415ecfaed7d2597555e202d85b3d65ea4e58d2d92ffa")));
        assertThat(nsec0, is("nsec1vl029mgpspedva04g90vltkh6fvh240zqtv9k0t9af8935ke9laqsnlfe5"));
    }

    @Test
    void itShouldConvertNoteSuccessfully() {
        // decode
        EventId eventId = Nip19.fromNote("note1s734rdpdmk27f920tzsjj2rtjnwf63n77ahtmtmdajtg6khx880q99zyw6");
        assertThat(eventId.toHex(), is("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de"));

        // encode
        String note0 = Nip19.toNote(EventId.fromHex("87a351b42ddd95e4954f58a129286b94dc9d467ef76ebdaf6dec968d5ae639de"));
        assertThat(note0, is("note1s734rdpdmk27f920tzsjj2rtjnwf63n77ahtmtmdajtg6khx880q99zyw6"));
    }

    @Test
    void itShouldConvertNprofileSuccessfully() {
        // decode
        Nip19.Nprofile nprofile = Nip19.fromNprofile("nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl0ne2cgukyawd24mayt8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uhkg6nzv9ejuumpv34kytnrdaksjlyr9p");

        assertThat(nprofile.getPublicKey().value.toHex(), is("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d"));

        assertThat(nprofile.getRelays(), hasSize(2));
        assertThat(nprofile.getRelays().get(0), is(RelayUri.fromString("wss://r.x.com")));
        assertThat(nprofile.getRelays().get(1), is(RelayUri.fromString("wss://djbas.sadkb.com")));
    }
}
