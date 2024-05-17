package org.tbk.nostr.proto.json;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;


class JsonRequestReaderTest {
    private static final Signer testSigner = SimpleSigner.fromPrivateKeyHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");

    /**
     * Rational: events must be verified anyway, so parsing "invalid" events without exception is fine.
     */
    @Test
    void itShouldReadEventRequest0() {
        Request request = JsonRequestReader.fromJson("""
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
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.EVENT));
        assertThat(request.getEvent(), is(EventRequest.newBuilder()
                .setEvent(MoreEvents.withEventId(Event.newBuilder()
                                .setCreatedAt(1)
                                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                                .setKind(1)
                                .setContent("GM"))
                        .build())
                .build()));
    }

    @Test
    void itShouldReadEventRequest1() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonRequestReader.fromJson("""
                    [
                      "EVENT",
                      {}
                    ]
                    """, Request.newBuilder());
        });
    }

    @Test
    void itShouldReadEventRequest2Fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonRequestReader.fromJson("""
                    [
                      "EVENT"
                    ]
                    """, Request.newBuilder());
        });
    }

    @Test
    void itShouldReadReqRequest0Simple() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                  },
                  {
                    "kinds": [1, 2, 21]
                  }
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.REQ));
        assertThat(request.getReq(), is(ReqRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder()
                        .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                        .build())
                .addFilters(Filter.newBuilder()
                        .addAllKinds(List.of(1, 2, 21))
                        .build())
                .build()));
    }

    @Test
    void itShouldReadReqRequest1() {
        Request request = JsonRequestReader.fromJson("""
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
                    "#alt": [ "only single letter tags should be parsed and end up in the object" ],
                    "since": 21,
                    "until": 42,
                    "limit": -1,
                    "search": "GM language:en",
                    "unknown_property": "should not end up in the object"
                  }
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.REQ));
        assertThat(request.getReq(), is(ReqRequest.newBuilder()
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
                        .addTags(MoreTags.filter(IndexedTag.Z,"test"))
                        .setSince(21)
                        .setUntil(42)
                        .setLimit(-1)
                        .setSearch("GM language:en")
                        .build())
                .build()));
    }

    @Test
    void itShouldReadReqRequest2RemoveDuplicatesInsideFilter() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "REQ",
                  "subscription_id",
                  {
                    "ids": [
                        "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                        "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "authors": [
                        "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                        "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                        "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"
                    ],
                    "kinds": [ 0, 1, 1, 1, 1, 1, 1 ],
                    "#e": [ "0", "1", "1", "1", "1", "1", "1" ],
                    "#E": [ "0", "2", "2", "2", "2", "2", "2", "2" ],
                    "#Z": [ "0", "test", "test", "test", "test", "test" ],
                    "#alt": [ "only single letter tags should be parsed and end up in the object" ],
                    "since": 21,
                    "until": 42,
                    "limit": -1,
                    "search": "GM language:en",
                    "unknown_property": "should not end up in the object"
                  },
                  {
                    "kinds": [1, 2, 21]
                  }
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.REQ));
        assertThat(request.getReq(), is(ReqRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder()
                        .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                        .addIds(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                        .addAuthors(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef"))
                        .addAuthors(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                        .addAllKinds(List.of(0, 1))
                        .addTags(MoreTags.filter(IndexedTag.e, "0", "1"))
                        .addTags(MoreTags.filter(IndexedTag.E, "0", "2"))
                        .addTags(MoreTags.filter(IndexedTag.Z, "0", "test"))
                        .setSince(21)
                        .setUntil(42)
                        .setLimit(-1)
                        .setSearch("GM language:en")
                        .build())
                .addFilters(Filter.newBuilder()
                        .addAllKinds(List.of(1, 2, 21))
                        .build())
                .build()));
    }

    @Test
    void itShouldReadReqRequest3() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "REQ",
                  "subscription_id",
                  {}
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.REQ));
        assertThat(request.getReq(), is(ReqRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder().build())
                .build()));
    }

    @Test
    void itShouldReadReqRequest4RemoveDuplicateFilters() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "REQ",
                  "subscription_id",
                  {}, {}, {},
                  {
                    "kinds": [1, 2, 21]
                  },
                  {
                    "search": "GM"
                  },
                  {
                    "ids": [
                      "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"
                    ]
                  },
                  {
                    "ids": [
                      "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                      "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36",
                      "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"
                    ]
                  },
                  {
                    "search": "GM"
                  },
                  {
                    "kinds": [1, 2, 21]
                  },
                  {}, {}, {}
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.REQ));
        assertThat(request.getReq(), is(ReqRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder().build())
                .addFilters(Filter.newBuilder()
                        .addAllKinds(List.of(1, 2, 21))
                        .build())
                .addFilters(Filter.newBuilder()
                        .setSearch("GM")
                        .build())
                .addFilters(Filter.newBuilder()
                        .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                        .build())
                .build()));
    }

    @Test
    void itShouldReadReqRequest5Fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonRequestReader.fromJson("""
                    [
                      "REQ",
                      "subscription_id"
                    ]
                    """, Request.newBuilder());
        });
    }

    @Test
    void itShouldReadCloseRequest0() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "CLOSE",
                  "subscription_id"
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.CLOSE));
        assertThat(request.getClose(), is(CloseRequest.newBuilder()
                .setId("subscription_id")
                .build()));
    }

    @Test
    void itShouldReadCountRequest0() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "COUNT",
                  "subscription_id",
                  {
                    "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                  }
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.COUNT));
        assertThat(request.getCount(), is(CountRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder()
                        .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                        .build())
                .build()));
    }

    @Test
    void itShouldReadAuthRequest0() {
        Request request = JsonRequestReader.fromJson("""
                [
                  "AUTH",
                  {
                    "id": "54510202f7e5bae8726048933a1d9833c0b865b5a306d311ed7f572a2bb66ba2",
                    "pubkey": "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                    "created_at" : 1,
                    "kind": 22242,
                    "tags": [
                      ["relay", "wss://relay.example.com/"],
                      ["challenge", "challengestringhere"]
                    ],
                    "content": "",
                    "sig": ""
                  }
                ]
                """, Request.newBuilder());

        assertThat(request.getKindCase(), is(Request.KindCase.AUTH));
        assertThat(request.getAuth(), is(AuthRequest.newBuilder()
                .setEvent(MoreEvents.withEventId(Event.newBuilder()
                                .setCreatedAt(1)
                                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                                .setKind(22_242)
                                .addTags(MoreTags.named("relay", "wss://relay.example.com/"))
                                .addTags(MoreTags.named("challenge", "challengestringhere"))
                        )
                        .build())
                .build()));
    }
}
