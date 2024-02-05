package org.tbk.nostr.util;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.nips.Nip1;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.json.JsonReader;

import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class MoreEventsTest {
    private static final Signer testSigner = SimpleSigner.fromHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");

    @Test
    void itShouldVerifyIdOfEvent0() {
        Event.Builder partialEvent = Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM");

        byte[] id = MoreEvents.eventId(partialEvent);

        assertThat(id, is(HexFormat.of().parseHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e")));
    }

    /**
     * {
     * "tags": [
     * ["e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"],
     * ["p", "f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"],
     * ["a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"],
     * ["alt", "reply"],
     * // ...
     * ],
     * // ...
     * }
     */
    @Test
    void itShouldVerifyIdOfEvent1() {
        Event.Builder partialEvent = Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .addTags(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"))
                .addTags(MoreTags.named("alt", "reply"))
                .setContent("GM");

        byte[] id = MoreEvents.eventId(partialEvent);

        assertThat(id, is(HexFormat.of().parseHex("5367ab9adc5c62d3fdbf9dd1d00807c921531db9925f906bf73b33afdf7f28dd")));
    }

    /**
     * <a href="https://njump.me/nevent1qqsv3erm28thj90lfgqvpvqwaj2k85443rxl5yahtf6828nlflkym8cpp4mhxue69uhkummn9ekx7mqpr4mhxue69uhkummnw3ez6ur4vgh8wetvd3hhyer9wghxuet5qy8hwumn8ghj7mn0wd68ytnddakszyrhwden5te0dehhxarj9emkjmn9qyf8wumn8ghj7mmxve3ksctfdch8qatzqy28wumn8ghj7un9d3shjtnyv9kh2uewd9hsz9nhwden5te0wfjkccte9ehx7um5wghxyctwvsq3gamnwvaz7tmjv4kxz7fwdehhxarj9e3xwqgkwaehxw309aex2mrp0yh8qunfd4skctnwv46qzymhwden5te0wfjkcctev93xcefwdaexwq3qsg6plzptd64u62a878hep2kev88swjh3tw00gjsfl8f237lmu63qxhztvw">
     * c8e47b51d77915ff4a00c0b00eec9563d2b588cdfa13b75a74751e7f4fec4d9f
     * </a>
     */
    @Test
    void itShouldVerifyExistingEvent0() {
        String json = """
                {
                  "id": "c8e47b51d77915ff4a00c0b00eec9563d2b588cdfa13b75a74751e7f4fec4d9f",
                  "pubkey": "82341f882b6eabcd2ba7f1ef90aad961cf074af15b9ef44a09f9d2a8fbfbe6a2",
                  "created_at": 1705851969,
                  "kind": 1,
                  "tags": [],
                  "content": "21",
                  "sig": "9e7fe4a10824a94d49163b02fab4dbc24253ba64464f151dddff7170ec6162f2cbf66bdf0dd62e00e6a9bea923bd8a9ef8231dcb4a34929e6294991f5d895c40"
                }
                """;

        Event event = JsonReader.fromJsonEvent(json);
        byte[] eventId = MoreEvents.eventId(event.toBuilder());

        assertThat(eventId, is(HexFormat.of().parseHex("c8e47b51d77915ff4a00c0b00eec9563d2b588cdfa13b75a74751e7f4fec4d9f")));

        Event verifiedEvent = MoreEvents.verify(event);
        assertThat(verifiedEvent, is(notNullValue()));
    }


    /**
     * <a href="https://njump.me/nevent1qqs87r7ljqsuhh5p2qrngrtq8dp7v8w7etzc5vm3vkt5ka8tssa6f0qppamhxue69uhkummnw3ezumt0d5q3samnwvaz7tmjv4kxz7fwwdhx7un59eek7cmfv9kqygyymmnwvah9hdnmft2wqsk0wr9as6q32hd4xk2zlnr2q5ectznjgqjyc4sc">
     * 7f0fdf9021cbde815007340d603b43e61ddecac58a337165974b74eb843ba4bc
     * </a>
     */
    @Test
    void itShouldVerifyExistingEvent1() {
        String json = """
                {
                  "id": "7f0fdf9021cbde815007340d603b43e61ddecac58a337165974b74eb843ba4bc",
                  "pubkey": "84dee6e676e5bb67b4ad4e042cf70cbd8681155db535942fcc6a0533858a7240",
                  "created_at": 1674508277,
                  "kind": 1,
                  "tags": [
                    ["e", "26a7df22030a4ad571ad7ae56466a96f7809228f2df99bc578cc399ab7bc0204", "wss://jiggytom.ddns.net", "reply"],
                    ["p", "84dee6e676e5bb67b4ad4e042cf70cbd8681155db535942fcc6a0533858a7240"]
                  ],
                  "content":"Oh wow okay you guys can stop sending. It works! Thank you!",
                  "sig":"cb1d152e61673d21a5ce2118f60572b4dccb5372287a735517b759f101899580c418a42db3386d8bfb66c3022dfe22d3373989390bebe453bf6279f06b6f1d34"
                }
                """;

        Event event = JsonReader.fromJsonEvent(json);
        byte[] eventId = MoreEvents.eventId(event.toBuilder());

        assertThat(eventId, is(HexFormat.of().parseHex("7f0fdf9021cbde815007340d603b43e61ddecac58a337165974b74eb843ba4bc")));

        Event verifiedEvent = MoreEvents.verify(event);
        assertThat(verifiedEvent, is(notNullValue()));
    }

    @Test
    void itShouldVerifyGeneratedEvent0() {
        Event event = MoreEvents.finalize(testSigner, Nip1.createTextNote(testSigner.getPublicKey(), "GM"));
        Event verifiedEvent = MoreEvents.verify(event);
        assertThat(verifiedEvent, is(notNullValue()));
    }

    @Test
    void itShouldVerifyGeneratedEvent1() {
        Event event = MoreEvents.createFinalizedTextNote(testSigner, "GM");
        Event verifiedEvent = MoreEvents.verify(event);
        assertThat(verifiedEvent, is(notNullValue()));
    }
}
