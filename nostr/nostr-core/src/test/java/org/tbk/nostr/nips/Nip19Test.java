package org.tbk.nostr.nips;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.util.MorePublicKeys;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Nip19Test {

    @Test
    void itShouldConvertNpubSuccessfully() {
        // decode
        XonlyPublicKey publicKey0 = Nip19.fromNpub("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg");
        assertThat(publicKey0.value.toHex(), is("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));

        // encode
        String npub0 = Nip19.toNpub(MorePublicKeys.fromHex("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));
        assertThat(npub0, is("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"));

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

}
