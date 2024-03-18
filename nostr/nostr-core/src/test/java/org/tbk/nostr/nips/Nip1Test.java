package org.tbk.nostr.nips;

import com.fasterxml.jackson.jr.ob.JSON;
import com.google.protobuf.ByteString;
import fr.acinq.bitcoin.XonlyPublicKey;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.Metadata;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

class Nip1Test {
    private static final XonlyPublicKey testPubkey = SimpleSigner.random().getPublicKey();

    @Test
    void itShouldCreateTextNote() {
        Instant now = Instant.now();

        Event event = Nip1.createTextNote(testPubkey, "GM").build();

        assertThat(event.getKind(), is(1));
        assertThat(event.getCreatedAt(), is(greaterThanOrEqualTo(now.getEpochSecond())));
        assertThat(event.getPubkey(), is(ByteString.fromHex(testPubkey.value.toHex())));
        assertThat(event.getContent(), is("GM"));
    }

    @Test
    void itShouldCreateMetadata() throws IOException {
        Instant now = Instant.now();
        Event event = Nip1.createMetadata(testPubkey, Metadata.newBuilder()
                        .name("name")
                        .about("about")
                        .picture(URI.create("https://www.example.com/example.png"))
                        .build())
                .build();

        assertThat(event.getKind(), is(0));
        assertThat(event.getCreatedAt(), is(greaterThanOrEqualTo(now.getEpochSecond())));
        assertThat(event.getPubkey(), is(ByteString.fromHex(testPubkey.value.toHex())));
        assertThat(JSON.std.anyFrom(event.getContent()), is(JSON.std.anyFrom("""
                {
                  "name": "name",
                  "about": "about",
                  "picture": "https://www.example.com/example.png",
                  "website": null,
                  "banner": null,
                  "display_name": null
                }
                """)));
    }
}
