package org.tbk.nostr.util;

import fr.acinq.bitcoin.ByteVector32;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.nips.Nip10;
import org.tbk.nostr.proto.TagValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
See: https://github.com/nostr-protocol/nips/blob/master/01.md
```json
{
  "tags": [
    ["e", "5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com"],
    ["p", "f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca"],
    ["a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com"],
    ["alt", "reply"],
    // ...
  ],
  // ...
}
```

and https://github.com/nostr-protocol/nips/blob/master/10.md
```json
["e", <event-id>, <relay-url>, <marker>]
```
 */
class MoreTagsTest {

    @Test
    void itShouldCreateETag() {
        TagValue tag0 = MoreTags.e(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), RelayUri.fromString("wss://nostr.example.com"));
        TagValue tag1 = MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com");

        assertThat(tag0, is(TagValue.newBuilder()
                .setName("e")
                .addValues("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")
                .addValues("wss://nostr.example.com")
                .build()));
        assertThat(tag1, is(tag0));
    }

    @Test
    void itShouldCreateETagWithNip10Marker0() {
        TagValue tag0 = MoreTags.e(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), Nip10.Marker.ROOT);
        TagValue tag1 = MoreTags.e(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), null, Nip10.Marker.ROOT);
        TagValue tag2 = Nip10.Marker.ROOT.tag(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), null);
        TagValue tag3 = Nip10.Marker.ROOT.tag(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"));
        TagValue tag4 = MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "", "root");

        assertThat(tag0, is(TagValue.newBuilder()
                .setName("e")
                .addValues("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")
                .addValues("")
                .addValues("root")
                .build()));
        assertThat(tag1, is(tag0));
        assertThat(tag2, is(tag0));
        assertThat(tag3, is(tag0));
        assertThat(tag4, is(tag0));
    }

    @Test
    void itShouldCreateETagWithNip10Marker1() {
        TagValue tag0 = MoreTags.e(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), RelayUri.fromString("wss://nostr.example.com"), Nip10.Marker.MENTION);
        TagValue tag1 = Nip10.Marker.MENTION.tag(EventId.fromHex("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36"), RelayUri.fromString("wss://nostr.example.com"));
        TagValue tag2 = MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com", "mention");

        assertThat(tag0, is(TagValue.newBuilder()
                .setName("e")
                .addValues("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")
                .addValues("wss://nostr.example.com")
                .addValues("mention")
                .build()));
        assertThat(tag1, is(tag0));
        assertThat(tag2, is(tag0));
    }

    @Test
    void itShouldCreatePTag() {
        TagValue tag0 = MoreTags.p(new XonlyPublicKey(ByteVector32.fromValidHex("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca")));
        TagValue tag1 = MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca");

        assertThat(tag0, is(TagValue.newBuilder()
                .setName("p")
                .addValues("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca")
                .build()));
        assertThat(tag1, is(tag0));
    }

    @Test
    void itShouldCreateATag() {
        TagValue tag = MoreTags.named("a", "30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd", "wss://nostr.example.com");

        assertThat(tag, is(TagValue.newBuilder()
                .setName("a")
                .addValues("30023:f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca:abcd")
                .addValues("wss://nostr.example.com")
                .build()));
    }


    @Test
    void itShouldCreateAltTag() {
        TagValue tag = MoreTags.named("alt", "reply");

        assertThat(tag, is(TagValue.newBuilder()
                .setName("alt")
                .addValues("reply")
                .build()));
    }
}