package org.tbk.nostr.proto.json;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.proto.*;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class JsonReaderTest {

    @Test
    void itShouldParseEventResponse0() {
        Response res = JsonReader.fromJsonResponse("""
                ["EVENT", "subscription_id", {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }]
                """);

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.EVENT));
        assertThat(res.getEvent(), is(notNullValue()));

        EventResponse eventResponse = res.getEvent();
        assertThat(eventResponse.getSubscriptionId(), is("subscription_id"));

        Event event = eventResponse.getEvent();
        assertThat(event.getId(), is(ByteString.fromHex("40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e")));
        assertThat(event.getPubkey(), is(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef")));
        assertThat(event.getKind(), is(1));
        assertThat(event.getContent(), is("GM"));
        assertThat(event.getSig(), is(ByteString.EMPTY));
    }

    @Test
    void itShouldParseEventResponse1() {
        Response res = JsonReader.fromJsonResponse("""
                ["EVENT", "subscription_id", {
                  "id" : "be5ef9d88cdc0a935e805894fb0feee59d048a6671d750d45ebd5de58a021763",
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
                }]
                """);

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.EVENT));
        assertThat(res.getEvent(), is(notNullValue()));

        EventResponse eventResponse = res.getEvent();
        assertThat(eventResponse.getSubscriptionId(), is("subscription_id"));

        Event event = eventResponse.getEvent();
        assertThat(event.getId(), is(ByteString.fromHex("be5ef9d88cdc0a935e805894fb0feee59d048a6671d750d45ebd5de58a021763")));
        assertThat(event.getPubkey(), is(ByteString.fromHex("493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef")));
        assertThat(event.getKind(), is(1));
        assertThat(event.getContent(), is("GM"));
        assertThat(event.getSig(), is(ByteString.EMPTY));
    }

    @Test
    void itShouldParseEventResponseWithEmptyId() {
        Event event = JsonReader.fromJsonEvent("""
                {
                  "id" : "",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """);

        assertThat(event, is(notNullValue()));
        assertThat(event.getId(), is(ByteString.EMPTY));
    }

    @Test
    void itShouldParseEventResponseWithInvalidId() {
        Event event = JsonReader.fromJsonEvent("""
                {
                  "id" : "c0ffeebabe",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """);

        assertThat(event, is(notNullValue()));
        assertThat(event.getId(), is(ByteString.fromHex("c0ffeebabe")));
    }

    @Test
    void itShouldParseEventResponseWithInvalidPubkey() {
        Event event = JsonReader.fromJsonEvent("""
                {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "c0ffeebabe",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """);

        assertThat(event, is(notNullValue()));
        assertThat(event.getPubkey(), is(ByteString.fromHex("c0ffeebabe")));
    }

    @Test
    void itShouldParseEventResponseWithMissingTags() {
        Event event = JsonReader.fromJsonEvent("""
                {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "content" : "GM",
                  "sig" : ""
                }
                """);

        assertThat(event, is(notNullValue()));
        assertThat(event.getTagsList(), is(Collections.emptyList()));
    }

    @Test
    void itShouldParseNoticeResponse() {
        Response res = JsonReader.fromJsonResponse("[\"NOTICE\", \"message\"]");

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.NOTICE));
        assertThat(res.getNotice(), is(notNullValue()));

        NoticeResponse notice = res.getNotice();
        assertThat(notice.getMessage(), is("message"));
    }

    @Test
    void itShouldParseClosedResponse() {
        Response res = JsonReader.fromJsonResponse("[\"CLOSED\", \"subscription_id\", \"message\"]");

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.CLOSED));
        assertThat(res.getClosed(), is(notNullValue()));

        ClosedResponse closed = res.getClosed();
        assertThat(closed.getSubscriptionId(), is("subscription_id"));
        assertThat(closed.getMessage(), is("message"));
    }

    @Test
    void itShouldParseEoseResponse() {
        Response res = JsonReader.fromJsonResponse("[\"EOSE\", \"subscription_id\"]");

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.EOSE));
        assertThat(res.getEose(), is(notNullValue()));

        EoseResponse eose = res.getEose();
        assertThat(eose.getSubscriptionId(), is("subscription_id"));
    }

    @Test
    void itShouldParseOkResponse() {
        Response res = JsonReader.fromJsonResponse("[\"OK\", \"5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36\", true, \"\"]");

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.OK));
        assertThat(res.getOk(), is(notNullValue()));

        OkResponse ok = res.getOk();
        assertThat(ok.getEventId(), is(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")));
        assertThat(ok.getSuccess(), is(true));
        assertThat(ok.getMessage(), is(""));
    }

    @Test
    void itShouldParseCountResponse0() {
        Response res = JsonReader.fromJsonResponse("""
                ["COUNT", "subscription_id", {
                  "count" : 21
                }]
                """);

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.COUNT));
        assertThat(res.getCount(), is(notNullValue()));

        CountResponse count = res.getCount();
        assertThat(count.getSubscriptionId(), is("subscription_id"));
        assertThat(count.getResult().getCount(), is(21L));
        assertThat(count.getResult().getApproximate(), is(false));
    }

    @Test
    void itShouldParseCountResponse1() {
        Response res = JsonReader.fromJsonResponse("""
                ["COUNT", "subscription_id", {
                  "count" : 42,
                  "approximate" : true
                }]
                """);

        assertThat(res, is(notNullValue()));
        assertThat(res.getKindCase(), is(Response.KindCase.COUNT));
        assertThat(res.getCount(), is(notNullValue()));

        CountResponse count = res.getCount();
        assertThat(count.getSubscriptionId(), is("subscription_id"));
        assertThat(count.getResult().getCount(), is(42L));
        assertThat(count.getResult().getApproximate(), is(true));
    }
}