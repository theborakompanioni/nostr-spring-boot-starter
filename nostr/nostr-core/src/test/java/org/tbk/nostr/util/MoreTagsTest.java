package org.tbk.nostr.util;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.proto.TagValue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/*
See: https://github.com/nostr-protocol/nips/blob/master/01.md

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
 */
class MoreTagsTest {

    @Test
    void itShouldCreateETag() {
        TagValue tag = MoreTags.e("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36", "wss://nostr.example.com");

        assertThat(tag, is(TagValue.newBuilder()
                .setName("e")
                .addValues("5c83da77af1dec6d7289834998ad7aafbd9e2191396d75ec3cc27f5a77226f36")
                .addValues("wss://nostr.example.com")
                .build()));
    }

    @Test
    void itShouldCreatePTag() {
        TagValue tag = MoreTags.p("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca");

        assertThat(tag, is(TagValue.newBuilder()
                .setName("p")
                .addValues("f7234bd4c1394dda46d09f35bd384dd30cc552ad5541990f98844fb06676e9ca")
                .build()));
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