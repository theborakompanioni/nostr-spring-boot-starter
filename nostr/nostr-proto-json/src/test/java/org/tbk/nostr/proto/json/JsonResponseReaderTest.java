package org.tbk.nostr.proto.json;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.proto.*;

import java.net.URI;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JsonResponseReaderTest {

    @Test
    void itShouldParseEventResponse0() {
        Response res = JsonReader.fromJson("""
                [ "EVENT", "subscription_id", {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                } ]
                """, Response.newBuilder());

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
        Response res = JsonReader.fromJson("""
                [ "EVENT", "subscription_id", {
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
                } ]
                """, Response.newBuilder());

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
    void itShouldFailToParseEmptyEvent() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonReader.fromJson("{}", Event.newBuilder());
        });
    }


    @Test
    void itShouldParseEventWithEmptyFields() {
        Event event = JsonReader.fromJson("""
                {
                  "id" : "",
                  "pubkey" : "",
                  "created_at" : 0,
                  "kind" : 0,
                  "tags" : [ ],
                  "content" : "",
                  "sig" : ""
                }
                """, Event.newBuilder());

        assertThat(event.getId(), is(ByteString.EMPTY));
    }

    @Test
    void itShouldParseEventWithEmptyId() {
        Event event = JsonReader.fromJson("""
                {
                  "id" : "",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """, Event.newBuilder());

        assertThat(event.getId(), is(ByteString.EMPTY));
    }

    @Test
    void itShouldParseEventWithInvalidId() {
        Event event = JsonReader.fromJson("""
                {
                  "id" : "c0ffeebabe",
                  "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """, Event.newBuilder());

        assertThat(event.getId(), is(ByteString.fromHex("c0ffeebabe")));
    }

    @Test
    void itShouldParseEventWithInvalidPubkey() {
        Event event = JsonReader.fromJson("""
                {
                  "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                  "pubkey" : "c0ffeebabe",
                  "created_at" : 1,
                  "kind" : 1,
                  "tags" : [ ],
                  "content" : "GM",
                  "sig" : ""
                }
                """, Event.newBuilder());

        assertThat(event.getPubkey(), is(ByteString.fromHex("c0ffeebabe")));
    }

    @Test
    void itShouldFailToParseEventWithProperty() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonReader.fromJson("""
                    {
                      "id" : "40a1d1223bc059a54185c097b4f6f352cf24e27a483fd60d39e635883a09091e",
                      "pubkey" : "493557ea5445d54298010d895d964e286c5d8fd704ac03823c6ddb0317643cef",
                      "created_at" : 1,
                      "kind" : 1,
                      "tags" : [ ],
                      "sig" : ""
                    }
                    """, Event.newBuilder());
        });
    }

    @Test
    void itShouldParseNoticeResponse() {
        Response res = JsonReader.fromJson("""
                [ "NOTICE", "message" ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.NOTICE));
        assertThat(res.getNotice(), is(notNullValue()));

        NoticeResponse notice = res.getNotice();
        assertThat(notice.getMessage(), is("message"));
    }

    @Test
    void itShouldParseClosedResponse() {
        Response res = JsonReader.fromJson("""
                [ "CLOSED", "subscription_id", "message" ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.CLOSED));
        assertThat(res.getClosed(), is(notNullValue()));

        ClosedResponse closed = res.getClosed();
        assertThat(closed.getSubscriptionId(), is("subscription_id"));
        assertThat(closed.getMessage(), is("message"));
    }

    @Test
    void itShouldParseEoseResponse() {
        Response res = JsonReader.fromJson("""
                [ "EOSE", "subscription_id" ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.EOSE));
        assertThat(res.getEose(), is(notNullValue()));

        EoseResponse eose = res.getEose();
        assertThat(eose.getSubscriptionId(), is("subscription_id"));
    }

    @Test
    void itShouldParseOkResponse() {
        Response res = JsonReader.fromJson("""
                [ "OK", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", true, "" ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.OK));
        assertThat(res.getOk(), is(notNullValue()));

        OkResponse ok = res.getOk();
        assertThat(ok.getEventId(), is(ByteString.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")));
        assertThat(ok.getSuccess(), is(true));
        assertThat(ok.getMessage(), is(""));
    }

    @Test
    void itShouldParseCountResponse0() {
        Response res = JsonReader.fromJson("""
                [ "COUNT", "subscription_id", {
                  "count" : 21
                } ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.COUNT));
        assertThat(res.getCount(), is(notNullValue()));

        CountResponse count = res.getCount();
        assertThat(count.getSubscriptionId(), is("subscription_id"));
        assertThat(count.getResult().getCount(), is(21L));
        assertThat(count.getResult().getApproximate(), is(false));
    }

    @Test
    void itShouldParseCountResponse1() {
        Response res = JsonReader.fromJson("""
                [ "COUNT", "subscription_id", {
                  "count" : 42,
                  "approximate" : true
                } ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.COUNT));
        assertThat(res.getCount(), is(notNullValue()));

        CountResponse count = res.getCount();
        assertThat(count.getSubscriptionId(), is("subscription_id"));
        assertThat(count.getResult().getCount(), is(42L));
        assertThat(count.getResult().getApproximate(), is(true));
    }

    @Test
    void itShouldParseAuthResponse0() {
        Response res = JsonReader.fromJson("""
                [
                  "AUTH",
                  "challenge"
                ]
                """, Response.newBuilder());

        assertThat(res.getKindCase(), is(Response.KindCase.AUTH));
        assertThat(res.getAuth(), is(notNullValue()));

        AuthResponse auth = res.getAuth();
        assertThat(auth.getChallenge(), is("challenge"));
    }

    @Test
    void itShouldParseMetadata0() {
        Metadata metadata = JsonReader.fromJson("""
                {
                  "name": "name",
                  "about": "about",
                  "picture": "https://www.example.com/example.png"
                }
                """, Metadata.newBuilder());

        assertThat(metadata.getName(), is("name"));
        assertThat(metadata.getAbout(), is("about"));
        assertThat(metadata.getPicture(), is(URI.create("https://www.example.com/example.png")));
        assertThat(metadata.getDisplayName(), is(nullValue()));
        assertThat(metadata.getWebsite(), is(nullValue()));
        assertThat(metadata.getBanner(), is(nullValue()));
        assertThat(metadata.getBot(), is(nullValue()));
        assertThat(metadata.getNip05(), is(nullValue()));
        assertThat(metadata.getLud16(), is(nullValue()));
    }

    @Test
    void itShouldParseMetadata1() {
        Metadata metadata = JsonReader.fromJson("""
                {
                  "name": "name",
                  "about": "about",
                  "picture": "https://www.example.com/picture.png",
                  "display_name": "display name",
                  "website": "https://www.example.com/",
                  "banner": "https://www.example.com/banner.png",
                  "bot": true,
                  "nip05": "nip05@example.com",
                  "lud16": "lud16@example.com"
                }
                """, Metadata.newBuilder());

        assertThat(metadata.getName(), is("name"));
        assertThat(metadata.getAbout(), is("about"));
        assertThat(metadata.getPicture(), is(URI.create("https://www.example.com/picture.png")));
        assertThat(metadata.getDisplayName(), is("display name"));
        assertThat(metadata.getWebsite(), is(URI.create("https://www.example.com/")));
        assertThat(metadata.getBanner(), is(URI.create("https://www.example.com/banner.png")));
        assertThat(metadata.getBot(), is(Boolean.TRUE));
        assertThat(metadata.getNip05(), is("nip05@example.com"));
        assertThat(metadata.getLud16(), is("lud16@example.com"));
    }

    @Test
    void itShouldParseMetadata2() {
        Metadata metadata0 = JsonReader.fromJson("{}", Metadata.newBuilder());

        assertThat(metadata0.getName(), is(nullValue()));
        assertThat(metadata0.getAbout(), is(nullValue()));
        assertThat(metadata0.getPicture(), is(nullValue()));
        assertThat(metadata0.getDisplayName(), is(nullValue()));
        assertThat(metadata0.getWebsite(), is(nullValue()));
        assertThat(metadata0.getBanner(), is(nullValue()));
        assertThat(metadata0.getBot(), is(nullValue()));
        assertThat(metadata0.getNip05(), is(nullValue()));
        assertThat(metadata0.getLud16(), is(nullValue()));

        Metadata metadata1 = JsonReader.fromJson("""
                {
                  "name": "name"
                }
                """, Metadata.newBuilder());

        assertThat(metadata1.getName(), is("name"));
        assertThat(metadata1.getAbout(), is(nullValue()));
        assertThat(metadata1.getPicture(), is(nullValue()));
        assertThat(metadata1.getDisplayName(), is(nullValue()));
        assertThat(metadata1.getWebsite(), is(nullValue()));
        assertThat(metadata1.getBanner(), is(nullValue()));
        assertThat(metadata1.getBot(), is(nullValue()));
        assertThat(metadata1.getNip05(), is(nullValue()));
        assertThat(metadata1.getLud16(), is(nullValue()));

        Metadata metadata2 = JsonReader.fromJson("""
                {
                  "about": "about"
                }
                """, Metadata.newBuilder());

        assertThat(metadata2.getName(), is(nullValue()));
        assertThat(metadata2.getAbout(), is("about"));
        assertThat(metadata2.getPicture(), is(nullValue()));
        assertThat(metadata2.getDisplayName(), is(nullValue()));
        assertThat(metadata2.getWebsite(), is(nullValue()));
        assertThat(metadata2.getBanner(), is(nullValue()));
        assertThat(metadata2.getBot(), is(nullValue()));
        assertThat(metadata2.getNip05(), is(nullValue()));
        assertThat(metadata2.getLud16(), is(nullValue()));


        Metadata metadata3 = JsonReader.fromJson("""
                {
                  "picture": "https://www.example.com/example.png"
                }
                """, Metadata.newBuilder());

        assertThat(metadata3.getName(), is(nullValue()));
        assertThat(metadata3.getAbout(), is(nullValue()));
        assertThat(metadata3.getPicture(), is(URI.create("https://www.example.com/example.png")));
        assertThat(metadata3.getDisplayName(), is(nullValue()));
        assertThat(metadata3.getWebsite(), is(nullValue()));
        assertThat(metadata3.getBanner(), is(nullValue()));
        assertThat(metadata3.getBot(), is(nullValue()));
        assertThat(metadata3.getNip05(), is(nullValue()));
        assertThat(metadata3.getLud16(), is(nullValue()));
    }

    @Test
    void itShouldParseMetadata3() {
        String json = """
                {
                  "id":"5bbb27cf3c5079cea00b1a4f5943f5790d7ed855fc4f3700514d58976574240b",
                  "pubkey":"6e468422dfb74a5738702a8823b9b28168abab8655faacb6853cd0ee15deee93",
                  "created_at":1714851835,
                  "kind":0,
                  "tags":[["alt","User profile for Gigi"]],
                  "content":"{\\"name\\":\\"Gigi\\",\\"nip05\\":\\"_@dergigi.com\\",\\"about\\":\\"not doing DMs â¢ not writing 21waysbook.com â¢ helping opensats.org â¢ painting twentyone.world â¢ maintaining bitcoin-resources.com & nostr-resources.com â¢ starting toomanyprojects.xyz ð¥\\",\\"lud16\\":\\"dergigi@primal.net\\",\\"display_name\\":\\"Gigi\\",\\"picture\\":\\"https://dergigi.com/assets/images/avatars/07.png\\",\\"banner\\":\\"https://cdn.nostr.build/i/0aeb7560c271bbb1cef00760989acd9dd3f37bdc42b37852eecb0d0b70a3e862.jpg\\",\\"website\\":\\"https://dergigi.com\\"}",
                  "sig":"a773117b6d866198474655559c44af86cedc230faac589e45399551151ad1125a94701c3008f32444ce332db647eddfd71a2c963f9ad88597cac98ebeaee304b"
                }
                """;

        Event event = JsonReader.fromJson(json, Event.newBuilder());

        Metadata metadata = JsonReader.fromJson(event.getContent(), Metadata.newBuilder());
        assertThat(metadata.getName(), is("Gigi"));
        assertThat(metadata.getAbout(), is(startsWith("not")));
        assertThat(metadata.getPicture(), is(URI.create("https://dergigi.com/assets/images/avatars/07.png")));
        assertThat(metadata.getDisplayName(), is("Gigi"));
        assertThat(metadata.getWebsite(), is(URI.create("https://dergigi.com")));
        assertThat(metadata.getBanner(), is(URI.create("https://cdn.nostr.build/i/0aeb7560c271bbb1cef00760989acd9dd3f37bdc42b37852eecb0d0b70a3e862.jpg")));
        assertThat(metadata.getBot(), is(nullValue()));
        assertThat(metadata.getNip05(), is("_@dergigi.com"));
        assertThat(metadata.getLud16(), is("dergigi@primal.net"));
    }

    @Test
    void itShouldParseMetadata3FailOnInvalidPictureUri() {
        assertThrows(IllegalArgumentException.class, () -> {
            JsonReader.fromJson("""
                    {
                      "picture": "not a valid uri"
                    }
                    """, Metadata.newBuilder());
        });
    }
}
