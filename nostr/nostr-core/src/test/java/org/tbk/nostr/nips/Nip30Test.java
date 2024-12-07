package org.tbk.nostr.nips;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class Nip30Test {

    @Test
    void isValidShortcodeSuccess() {
        assertThat(Nip30.isValidShortcode("a"), is(true));
        assertThat(Nip30.isValidShortcode("_"), is(true));
        assertThat(Nip30.isValidShortcode("a_z"), is(true));
        assertThat(Nip30.isValidShortcode("_a_"), is(true));
        assertThat(Nip30.isValidShortcode("soapbox"), is(true));
        assertThat(Nip30.isValidShortcode("abcABC_0123456789"), is(true));
    }

    @Test
    void isValidShortcodeFail() {
        assertThat(Nip30.isValidShortcode(""), is(false));
        assertThat(Nip30.isValidShortcode(":"), is(false));
        assertThat(Nip30.isValidShortcode("soapbox!"), is(false));
        assertThat(Nip30.isValidShortcode("soap box"), is(false));
    }

    @Test
    void emojiSuccess() {
        TagValue emoji = Nip30.emojiTag("soapbox", URI.create("https://gleasonator.com/emoji/Gleasonator/soapbox.png"));

        assertThat(emoji.getName(), is("emoji"));
        assertThat(emoji.getValuesCount(), is(2));
        assertThat(emoji.getValues(0), is("soapbox"));
        assertThat(emoji.getValues(1), is("https://gleasonator.com/emoji/Gleasonator/soapbox.png"));
    }

    @Test
    void emojiFail() {
        IllegalArgumentException error = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Nip30.emojiTag("!", URI.create("https://example.org/!.png"));
        });

        assertThat(error.getMessage(), is("Illegal characters in shortcode."));
    }

    @Test
    void parse() {
        Event.Builder event = JsonReader.fromJsonPartial("""
                {
                  "kind": 1,
                  "content": "Hello :gleasonator: 😂 :ablobcatrainbow: :disputed: yolo",
                  "tags": [
                    ["emoji", "ablobcatrainbow", "https://gleasonator.com/emoji/blobcat/ablobcatrainbow.png"],
                    ["emoji", "disputed", "https://gleasonator.com/emoji/Fun/disputed.png"],
                    ["emoji", "gleasonator", "https://gleasonator.com/emoji/Gleasonator/gleasonator.png"]
                  ],
                  "pubkey": "79c2cae114ea28a981e7559b4fe7854a473521a8d22a66bbab9fa248eb820ff6",
                  "created_at": 1682630000
                }
                """, Event.newBuilder());

        List<TagValue> emojiTags = MoreTags.findByName(event, "emoji");
        assertThat(emojiTags, hasSize(3));
        assertThat(emojiTags.get(0).getValues(0), is("ablobcatrainbow"));
        assertThat(emojiTags.get(1).getValues(0), is("disputed"));
        assertThat(emojiTags.get(2).getValues(0), is("gleasonator"));
    }
}
