package org.tbk.nostr.nip19;

import fr.acinq.bitcoin.PrivateKey;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.Kind;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.util.MoreEvents;
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
        assertThat(publicKey0, is(MorePublicKeys.fromHex("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e")));

        // encode0
        String npub0 = Nip19.toNpub(MorePublicKeys.fromHex("7e7e9c42a91bfef19fa929e5fda1b72e0ebc1a4c1141673e2794234d86addf4e"));
        assertThat(npub0, is("npub10elfcs4fr0l0r8af98jlmgdh9c8tcxjvz9qkw038js35mp4dma8qzvjptg"));

        // decode1
        XonlyPublicKey publicKey1 = Nip19.fromNpub("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6");
        assertThat(publicKey1, is(MorePublicKeys.fromHex("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d")));

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
        Nip19.Nprofile nprofile = Nip19.fromNprofile("nprofile1qqsrhuxx8l9ex335q7he0f09aej04zpazpl0ne2cgukyawd24mayt8gpp4mhxue69uhhytnc9e3k7mgpz4mhxue69uhkg6nzv9ejuumpv34kytnrdaksjlyr9p");

        assertThat(nprofile.getPublicKey(), is(MorePublicKeys.fromHex("3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d")));

        assertThat(nprofile.getRelays(), hasSize(2));
        assertThat(nprofile.getRelays().get(0), is(RelayUri.fromString("wss://r.x.com")));
        assertThat(nprofile.getRelays().get(1), is(RelayUri.fromString("wss://djbas.sadkb.com")));
    }

    @Test
    void itShouldConvertNeventSuccessfully0() {
        String neventEncoded = "nevent1qvzqqqqqqypzp7wadfmz2p3xpvu295l9k3jzz0pwglarsa6znl57uc8qwx335p7hqyvhwumn8ghj7enfv96x5ctx9ehx7um5wgcjucm0d5hszymhwden5te0dehhxarj9ehkumewwfjj7qgcwaehxw309ahx7um5wghx6ctnwdkh27pwvdhk6tcpzemhxue69uhkzat5dqhxummnw3erztnrdakj7qgewaehxw309ahx7um5wgh8yctyd9u8yct59e3k7mf0qqsp8vh0ggzy5smdstg6vxv8rfmhhwjltuekj6fq6m7p8n9uujmev6qxt0hez";
        Nip19.Nevent nevent = Nip19.fromNevent(neventEncoded);

        assertThat(nevent.getEventId(), is(EventId.fromHex("13b2ef42044a436d82d1a619871a777bba5f5f33696920d6fc13ccbce4b79668")));
        assertThat(nevent.getPublicKey().isPresent(), is(true));
        assertThat(nevent.getPublicKey().orElseThrow(), is(MorePublicKeys.fromHex("f9dd6a762506260b38a2d3e5b464213c2e47fa3877429fe9ee60e071a31a07d7")));

        assertThat(nevent.getRelays(), hasSize(5));
        assertThat(nevent.getRelays().get(0), is(RelayUri.fromString("wss://fiatjaf.nostr1.com/")));
        assertThat(nevent.getRelays().get(1), is(RelayUri.fromString("wss://nostr.ono.re/")));
        assertThat(nevent.getRelays().get(2), is(RelayUri.fromString("wss://nostr.massmux.com/")));
        assertThat(nevent.getRelays().get(3), is(RelayUri.fromString("wss://auth.nostr1.com/")));
        assertThat(nevent.getRelays().get(4), is(RelayUri.fromString("wss://nostr.radixrat.com/")));

        assertThat(nevent.getKind().isPresent(), is(true));
        assertThat(nevent.getKind().orElseThrow(), is(Kind.of(1)));

        // we are using a different order of the TLV values, so the encoded string does not match
        // let's only check for object equality
        assertThat(Nip19.fromNevent(Nip19.toNevent(nevent)), is(nevent));
    }

    @Test
    void itShouldConvertNeventSuccessfully1Minimal() {
        String neventEncoded = "nevent1qqsgas23d6gvwlv90g5pg4jpacsy55h2ag2ylk8pwp7dfleutknl0tsav9sc2";
        Nip19.Nevent nevent = Nip19.fromNevent(neventEncoded);

        assertThat(nevent.getEventId(), is(EventId.fromHex("8ec1516e90c77d857a28145641ee204a52eaea144fd8e1707cd4ff3c5da7f7ae")));
        assertThat(nevent.getPublicKey().isPresent(), is(false));

        assertThat(nevent.getRelays(), hasSize(0));

        assertThat(nevent.getKind().isPresent(), is(false));

        // encode
        assertThat(Nip19.toNevent(nevent), is(neventEncoded));
    }

    @Test
    void itShouldConvertNeventSuccessfully2NoPublicKey() {
        String neventEncoded = "nevent1qqsy457zer9nuep8cjav6284stkr7qsk65t8jl8vf7e8f9ga0qmne6qprdmhxue69uhhyetvv9ujuam9wd6x2unwvf6xxtnrdakj7qexlv3";
        Nip19.Nevent nevent = Nip19.fromNevent(neventEncoded);

        assertThat(nevent.getEventId(), is(EventId.fromHex("4ad3c2c8cb3e6427c4bacd28f582ec3f0216d516797cec4fb274951d78373ce8")));
        assertThat(nevent.getPublicKey().isPresent(), is(false));

        assertThat(nevent.getRelays(), hasSize(1));
        assertThat(nevent.getRelays().get(0), is(RelayUri.fromString("wss://relay.westernbtc.com/")));

        assertThat(nevent.getKind().isPresent(), is(false));

        // encode
        assertThat(Nip19.toNevent(nevent), is(neventEncoded));
    }

    @Test
    void itShouldFailToConvertInvalidNevent() {
        // this nevent string is missing the special (type := 0) TLV entry
        String invalidNeventString = "nevent1qgsqlkuslr3rf56qpmd0m5ndfyl39m7q6l0zcmuly8ue0praxwkjagcpz3mhxue69uhhyetvv9ujuerpd46hxtnfduqs6amnwvaz7tmwdaejumr0dspsgqqqqqqs03k8v3";

        try {
            Nip19.Nevent ignoredOnPurpose = Nip19.fromNevent(invalidNeventString);
            Assertions.fail("Should have thrown exception");
        } catch (Exception e) {
            assertThat(e.getMessage(), is("Error while decoding bech32"));
            assertThat(e.getCause().getMessage(), is("Decoding failed: No value with type 0."));
        }
    }

    @Test
    void itShouldEncodeEventAsNevent0() {
        SimpleSigner signer = SimpleSigner.fromIdentity(Persona.alice());

        Event event = MoreEvents.finalize(signer, Nip1.createTextNote(signer.getPublicKey(), "GM")
                .setCreatedAt(1));

        String neventEncoded = Nip19.toNevent(event);
        assertThat(neventEncoded, is("nevent1qqsqmtm0ave40kvaw72e4a04ed0evcy6fnsc7latu9wtpl24r3c6avqzyre3jf56sat7sn5mdkkexfwtwjfn7e8ff97ycw50watnv8ncah6kgqcyqqqqqqgrnkc3n"));

        Nip19.Nevent nevent = Nip19.fromNevent(neventEncoded);
        assertThat(nevent.getEventId(), is(EventId.of(event.getId().toByteArray())));
        assertThat(nevent.getRelays(), hasSize(0));
        assertThat(nevent.getPublicKey().orElseThrow(), is(MorePublicKeys.fromBytes(event.getPubkey().toByteArray())));
        assertThat(nevent.getKind().orElseThrow(), is(Kind.of(event.getKind())));
    }

    @Test
    void itShouldConvertNaddrSuccessfully() {
        String naddrEncoded = "naddr1qqyk67tswfhkv6tvv5pzqwlsccluhy6xxsr6l9a9uhhxf75g85g8a709tprjcn4e42h053vaqvzqqqqy6gq3zamnwvaz7tm90psk6urvv5hxxmmdpmcy2t";
        Nip19.Naddr naddr = Nip19.fromNaddr(naddrEncoded);

        assertThat(naddr.getUri().toString(), is("1234:3bf0c63fcb93463407af97a5e5ee64fa883d107ef9e558472c4eb9aaaefa459d:myprofile"));

        assertThat(naddr.getRelays(), hasSize(1));
        assertThat(naddr.getRelays().get(0), is(RelayUri.fromString("wss://example.com")));
    }
}
