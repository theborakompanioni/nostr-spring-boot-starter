package org.tbk.nostr.nips;

import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.net.URI;
import java.util.HexFormat;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class Nip98Test {

    @Test
    void itShouldCreateAuthEvent() {
        Signer signer = SimpleSigner.random();

        Event event = MoreEvents.finalize(signer, Nip98.createAuthEvent(signer.getPublicKey(),
                URI.create("https://api.snort.social/api/v1/n5sp/list"),
                "GET"));

        assertThat(event.getKind(), is(Kinds.kindHTTPAuth.getValue()));
        assertThat(event.getContent(), is(""));

        TagValue uTag = MoreTags.findByNameSingle(event, IndexedTag.u).orElseThrow();
        assertThat(uTag.getValuesList().getFirst(), is("https://api.snort.social/api/v1/n5sp/list"));

        TagValue methodTag = MoreTags.findByNameSingle(event, "method").orElseThrow();
        assertThat(methodTag.getValuesList().getFirst(), is("GET"));
    }

    @Test
    void itShouldCreateAuthEventWithBody() {
        Signer signer = SimpleSigner.random();

        String body = "{}";
        String bodyHash = "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a";
        assertThat("sanity check", bodyHash, is(Nip98.payloadTagFromBody(body).getValues(0)));

        Event event = MoreEvents.finalize(signer, Nip98.createAuthEventWithBody(signer.getPublicKey(),
                URI.create("https://api.snort.social/api/v1/n5sp/list"),
                "POST",
                body));

        assertThat(event.getKind(), is(Kinds.kindHTTPAuth.getValue()));
        assertThat(event.getContent(), is(""));

        TagValue uTag = MoreTags.findByNameSingle(event, IndexedTag.u).orElseThrow();
        assertThat(uTag.getValues(0), is("https://api.snort.social/api/v1/n5sp/list"));

        TagValue methodTag = MoreTags.findByNameSingle(event, "method").orElseThrow();
        assertThat(methodTag.getValues(0), is("POST"));

        TagValue payloadTag = MoreTags.findByNameSingle(event, "payload").orElseThrow();
        assertThat(payloadTag.getValues(0), is(bodyHash));
    }
}
