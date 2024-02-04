package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.PrivateKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.MoreIdentities;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.EventRequest;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class JsonWriterTest {
    private static final PrivateKey testPrivateKey = MoreIdentities.fromHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");

    @Test
    void itShouldWriteEventRequest() throws IOException {
        Event.Builder partialEvent = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testPrivateKey.xOnlyPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM"));

        EventRequest eventRequest = EventRequest.newBuilder()
                .setEvent(partialEvent.build())
                .build();

        String json = JsonWriter.toJson(eventRequest);

        assertThat(json, is(notNullValue()));
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
    void itShouldWriteReqRequest0() throws IOException {
        ReqRequest reqRequest = ReqRequest.newBuilder()
                .setId("subscription_id")
                .addFilters(Filter.newBuilder().addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")).build())
                .build();

        String json = JsonWriter.toJson(reqRequest);

        assertThat(json, is(notNullValue()));
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
    void itShouldWriteEvent0() throws IOException {
        Event.Builder partialEvent = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testPrivateKey.xOnlyPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM"));

        String json = JsonWriter.toJson(partialEvent.build());

        assertThat(json, is(notNullValue()));
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
                .setPubkey(ByteString.fromHex(testPrivateKey.xOnlyPublicKey().value.toHex()))
                .setKind(1)
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .addTags(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"))
                .addTags(MoreTags.named("alt", "reply"))
                .setContent("GM"));

        String json = JsonWriter.toJson(partialEvent.build());

        assertThat(json, is(notNullValue()));
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
                .setPubkey(ByteString.fromHex(testPrivateKey.xOnlyPublicKey().value.toHex()))
                .setKind(1)
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", "root"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", ""))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .setContent("GM"));

        String json = JsonWriter.toJson(partialEvent.build());

        assertThat(json, is(notNullValue()));
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
        Filter filter = Filter.newBuilder()
                .addIds(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .build();

        String json = JsonWriter.toJson(filter);

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "ids": ["5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"]
                }
                """)));
    }

    @Test
    void itShouldWriteFilter1() throws IOException {
        Filter filter = Filter.newBuilder()
                .addAuthors(ByteString.fromHex("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .build();

        String json = JsonWriter.toJson(filter);

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "authors": ["f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"]
                }
                """)));
    }

    @Test
    void itShouldWriteFilter2() throws IOException {
        Filter filter = Filter.newBuilder()
                .setSince(1)
                .setUntil(21)
                .setLimit(42)
                .build();

        String json = JsonWriter.toJson(filter);

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
        Filter filter = Filter.newBuilder().build();

        String json = JsonWriter.toJson(filter);

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("{}")));
    }

    @Test
    void itShouldWriteFilter4() throws IOException {
        Filter filter = Filter.newBuilder()
                .addTags(MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"))
                .addTags(MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"))
                .addTags(MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd"))
                .addTags(MoreTags.named("alt", "reply")) // will be ignored - not an "indexed tag" (single char)
                .build();

        String json = JsonWriter.toJson(filter);

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
        Filter filter = Filter.newBuilder()
                .addTags(MoreTags.e("e0", "e1"))
                .addTags(MoreTags.e("e2"))
                .addTags(MoreTags.p("p0", "p1"))
                .addTags(MoreTags.p("p2"))
                .addTags(MoreTags.named("Z", "Z0", "Z1"))
                .addTags(MoreTags.named("Z", "Z3"))
                .build();

        String json = JsonWriter.toJson(filter);

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                {
                  "#e": ["e0", "e1", "e2"],
                  "#p": ["p0", "p1", "p2"],
                  "#Z": ["Z0", "Z1", "Z3"]
                }
                """)));
    }
}
