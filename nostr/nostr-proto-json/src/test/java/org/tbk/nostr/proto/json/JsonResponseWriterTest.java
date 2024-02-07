package org.tbk.nostr.proto.json;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.*;
import org.tbk.nostr.util.MoreEvents;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class JsonResponseWriterTest {
    private static final Signer testSigner = SimpleSigner.fromPrivateKeyHex("958c7ed568943914f3763e1034883710d8d33eb2ad20b41b0db7babff50a238e");

    @Test
    void itShouldWriteNoticeResponse0() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder()
                        .setMessage("test")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "NOTICE",
                  "test"
                ]
                """)));
    }

    @Test
    void itShouldWriteNoticeResponse1() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setNotice(NoticeResponse.newBuilder().build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "NOTICE",
                  ""
                ]
                """)));
    }

    @Test
    void itShouldWriteClosedResponse0() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId("subscription_id")
                        .setMessage("test")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "CLOSED",
                  "subscription_id",
                  "test"
                ]
                """)));
    }

    @Test
    void itShouldWriteClosedResponse1() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setClosed(ClosedResponse.newBuilder()
                        .setSubscriptionId("subscription_id")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "CLOSED",
                  "subscription_id",
                  ""
                ]
                """)));
    }

    @Test
    void itShouldWriteEventResponse() throws IOException {
        Event.Builder partialEvent = MoreEvents.withEventId(Event.newBuilder()
                .setCreatedAt(1)
                .setPubkey(ByteString.fromHex(testSigner.getPublicKey().value.toHex()))
                .setKind(1)
                .setContent("GM"));

        String json = JsonWriter.toJson(Response.newBuilder()
                .setEvent(EventResponse.newBuilder()
                        .setSubscriptionId("subscription_id")
                        .setEvent(partialEvent.build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "EVENT",
                  "subscription_id",
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
    void itShouldWriteCountResponse0() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setCount(CountResponse.newBuilder()
                        .setSubscriptionId("subscription_id")
                        .setResult(CountResult.newBuilder()
                                .setCount(21)
                                .setApproximate(true)
                                .build())
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "COUNT",
                  "subscription_id",
                  {
                    "count": 21,
                    "approximate": true
                  }
                ]
                """)));
    }

    @Test
    void itShouldWriteEoseResponse0() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setEose(EoseResponse.newBuilder()
                        .setSubscriptionId("subscription_id")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "EOSE",
                  "subscription_id"
                ]
                """)));
    }

    @Test
    void itShouldWriteOkResponse0() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                        .setSuccess(true)
                        .setMessage("test")
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "OK",
                  "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  true,
                  "test"
                ]
                """)));
    }

    @Test
    void itShouldWriteOkResponse1() throws IOException {
        String json = JsonWriter.toJson(Response.newBuilder()
                .setOk(OkResponse.newBuilder()
                        .setEventId(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e"))
                        .setSuccess(true)
                        .build())
                .build());

        assertThat(JSON.std.anyFrom(json), is(JSON.std.anyFrom("""
                [
                  "OK",
                  "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  true,
                  ""
                ]
                """)));
    }
}
