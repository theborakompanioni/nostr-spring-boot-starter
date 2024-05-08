package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JsonRequestWriterTest {
    private static final Signer testSigner = SimpleSigner.fromPrivateKeyHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");

    @Test
    void itShouldWriteEventRequest0() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setEvent(EventRequest.newBuilder()
                        .setEvent(MoreEvents.withEventId(Event.newBuilder()
                                        .setCreatedAt(1)
                                        .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                                        .setKind(1)
                                        .setContent("GM"))
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "EVENT",
                  {
                    "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                    "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                    "created_at" : 1,
                    "kind" : 1,
                    "tags" : [ ],
                    "content" : "GM",
                    "sig" : ""
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteEventRequest1() throws IOException {
        Event event = MoreEvents.finalize(testSigner, Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM"));

        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setEvent(EventRequest.newBuilder()
                        .setEvent(event)
                        .build())
                .build());

        String sig = HexFormat.of().formatHex(event.getSig().toByteArray());
        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "EVENT",
                  {
                    "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                    "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                    "created_at" : 1,
                    "kind" : 1,
                    "tags" : [ ],
                    "content" : "GM",
                    "sig" : "%s"
                  }
                ]
                """.formatted(sig))));
    }

    @Test
    void itShouldWriteReqRequest0() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setReq(ReqRequest.newBuilder()
                        .setId("subscription_id")
                        .addFilters(Filter.newBuilder()
                                .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteReqRequest1() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setReq(ReqRequest.newBuilder()
                        .setId("subscription_id")
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"))
                                .setLimit(21)
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "authors": ["493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"],
                    "limit": 21
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteReqRequest2SkipUnnecessaryValues() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setReq(ReqRequest.newBuilder()
                        .setId("subscription_id")
                        .addFilters(Filter.newBuilder()
                                .addAuthors(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"))
                                .addAllKinds(Collections.emptyList())
                                .addTags(TagFilter.newBuilder()
                                        .setName("alt")
                                        .addValues("only single letter tags should be serialized and end up in the object")
                                        .build())
                                .setSince(-1)
                                .setUntil(-1)
                                .setLimit(-1)
                                .setSearch("")
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "authors": ["493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"]
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteReqRequest3() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setReq(ReqRequest.newBuilder()
                        .setId("subscription_id")
                        .addFilters(Filter.newBuilder()
                                .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                                .addIds(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                                .addAuthors(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"))
                                .addAuthors(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                                .addAllKinds(List.of(-1, 0, 1, 21))
                                .addTags(MoreTags.filter(IndexedTag.e,
                                        "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                                ))
                                .addTags(MoreTags.filter(IndexedTag.p,
                                        "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                                ))
                                .addTags(MoreTags.filter(IndexedTag.a,
                                        "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd",
                                        "12345:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:test"
                                ))
                                .addTags(MoreTags.filter(IndexedTag.Z, "test"))
                                .addTags(TagFilter.newBuilder()
                                        .setName("alt")
                                        .addValues("only single letter tags should be serialized and end up in the object")
                                        .build())
                                .setSince(21)
                                .setUntil(42)
                                .setLimit(1)
                                .setSearch("GM language:en")
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "ids": [
                        "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "authors": [
                        "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "kinds": [ -1, 0, 1, 21 ],
                    "#e": [
                        "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "#p": [
                        "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "#a": [
                        "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd",
                        "12345:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:test"
                    ],
                    "#Z": [ "test" ],
                    "since": 21,
                    "until": 42,
                    "limit": 1,
                    "search": "GM language:en"
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteCloseRequest0() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setClose(CloseRequest.newBuilder()
                        .setId("subscription_id")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "CLOSE",
                  "subscription_id"
                ]
                """)));
    }

    @Test
    void itShouldWriteCountRequest0() throws IOException {
        String json = JsonRequestWriter.toJson(Request.newBuilder()
                .setCount(CountRequest.newBuilder()
                        .setId("subscription_id")
                        .addFilters(Filter.newBuilder()
                                .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "COUNT",
                  "subscription_id",
                  {
                    "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteEvent0() throws IOException {
        String json = JsonRequestWriter.toJson(MoreEvents.withEventId(Event.newBuilder()
                        .setCreatedAt(1)
                        .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                        .setKind(1)
                        .setContent("GM"))
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """)));
    }

    @Test
    void itShouldWriteEvent1() throws IOException {
        Event.Builder partialEvent = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .addTags(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"))
                .addTags(MoreTags.named("alt", "reply"))
                .setContent("GM"));

        String json = JsonRequestWriter.toJson(partialEvent.build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "id" : "5367ab9adc5c62d3fdbf9dd1d00807c921531db9925f906bf73b33afdf7f28dd",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com" ],
                    [ "p", "f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca" ],
                    [ "a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com" ],
                    [ "alt", "reply" ]
                  ],
                  "content" : "GM",
                  "sig" : ""
                }
                """)));
    }

    @Test
    void itShouldWriteEvent2WithMultipleDuplicateTags() throws IOException {
        Event.Builder partialEvent = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", "root"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", ""))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .setContent("GM"));

        String json = JsonRequestWriter.toJson(partialEvent.build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "id" : "a967de9634c1ae242e066ed421ff7de537608e3e8d34d982b363f30979267c50",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", "root" ],
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", "" ],
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com" ],
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36" ],
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36" ],
                    [ "e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36" ]
                  ],
                  "content" : "GM",
                  "sig" : ""
                }
                """)));
    }

    @Test
    void itShouldWriteFilter0() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder()
                .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                }
                """)));
    }

    @Test
    void itShouldWriteFilter1() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder()
                .addAuthors(ByteString.fromHex("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "authors": ["f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"]
                }
                """)));
    }

    @Test
    void itShouldWriteFilter2() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder()
                .setSince(1)
                .setUntil(21)
                .setLimit(42)
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "since": 1,
                  "until": 21,
                  "limit": 42
                }
                """)));
    }

    /**
     * Not sure if it should be possible to serialize an empty filter.
     * Currently, it is safe to assume, users know what they are doing.
     */
    @Test
    void itShouldWriteFilter3Empty() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder().build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("{}")));
    }

    @Test
    void itShouldWriteFilter4() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder()
                .addTags(MoreTags.filter(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")))
                .addTags(MoreTags.filter(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca")))
                .addTags(MoreTags.filter(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd")))
                .addTags(TagFilter.newBuilder()
                        .setName("alt")  // will be ignored - not an "indexed tag" (single char)
                        .addValues("reply")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "#e": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"],
                  "#p": ["f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"],
                  "#a": ["30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd"]
                }
                """)));
    }

    @Test
    void itShouldWriteFilter5() throws IOException {
        String json = JsonRequestWriter.toJson(Filter.newBuilder()
                .addTags(MoreTags.filter(IndexedTag.e, "e0", "e1"))
                .addTags(MoreTags.filter(IndexedTag.e, "e2"))
                .addTags(MoreTags.filter(IndexedTag.p, "p0", "p1"))
                .addTags(MoreTags.filter(IndexedTag.p, "p2"))
                .addTags(MoreTags.filter(IndexedTag.Z, "Z0", "Z1"))
                .addTags(MoreTags.filter(IndexedTag.Z, "Z3"))
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "#e": ["e0", "e1", "e2"],
                  "#p": ["p0", "p1", "p2"],
                  "#Z": ["Z0", "Z1", "Z3"]
                }
                """)));
    }

    @Test
    void itShouldWriteMetadata0() throws IOException {
        String json = JsonRequestWriter.toJson(Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/example.png"))
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "name": "name",
                  "about": "about",
                  "picture": "https://www.example.com/example.png",
                  "website": null,
                  "banner": null,
                  "display_name": null,
                  "bot": false
                }
                """)));
    }

    @Test
    void itShouldWriteMetadata1() throws IOException {
        String json = JsonRequestWriter.toJson(Metadata.newBuilder()
                .name("name")
                .about("about")
                .picture(URI.create("https://www.example.com/picture.png"))
                .displayName("display name")
                .website(URI.create("https://www.example.com/"))
                .banner(URI.create("https://www.example.com/banner.png"))
                .bot(true)
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "name": "name",
                  "about": "about",
                  "picture": "https://www.example.com/picture.png",
                  "display_name": "display name",
                  "website": "https://www.example.com/",
                  "banner": "https://www.example.com/banner.png",
                  "bot": true
                }
                """)));
    }
}
