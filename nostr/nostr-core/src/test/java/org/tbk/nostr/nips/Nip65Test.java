package org.tbk.nostr.nips;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;
import org.tbk.nostr.base.IndexedTag;
import org.tbk.nostr.base.RelayUri;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.TagValue;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreTags;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class Nip65Test {

    @Test
    void itShouldCreateRelayListEvent0Empty() {
        Signer signer = SimpleSigner.random();

        Event event0 = MoreEvents.finalize(signer, Nip65.createRelayListEvent(signer.getPublicKey(), Set.of()));

        List<TagValue> rTag = MoreTags.findByName(event0, IndexedTag.r);
        assertThat(rTag, hasSize(0));

        assertThat(Nip65.findRelays(event0).getWriteRelays(), hasSize(0));
        assertThat(Nip65.findRelays(event0).getWriteRelays(), hasSize(0));
    }

    @Test
    void itShouldCreateRelayListEvent1() {
        Signer signer = SimpleSigner.random();

        Set<RelayUri> readRelays = Set.of(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8081"));
        Set<RelayUri> writeRelays = Set.of(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8082"));
        Event event0 = MoreEvents.finalize(signer, Nip65.createRelayListEvent(signer.getPublicKey(), Nip65.ReadWriteRelays.builder()
                .readRelays(readRelays)
                .writeRelays(writeRelays)
                .build()));

        List<TagValue> rTag = MoreTags.findByName(event0, IndexedTag.r);
        assertThat(rTag, hasSize(3));

        assertThat(rTag.get(0).getValuesCount(), is(1));
        assertThat(rTag.get(0).getValues(0), is("ws://localhost:8080"));

        assertThat(rTag.get(1).getValuesCount(), is(2));
        assertThat(rTag.get(1).getValues(0), is("ws://localhost:8081"));
        assertThat(rTag.get(1).getValues(1), is("read"));

        assertThat(rTag.get(2).getValuesCount(), is(2));
        assertThat(rTag.get(2).getValues(0), is("ws://localhost:8082"));
        assertThat(rTag.get(2).getValues(1), is("write"));

        Nip65.ReadWriteRelays relays = Nip65.findRelays(event0);
        assertThat(relays.getReadRelays(), containsInRelativeOrder(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8081")));
        assertThat(relays.getWriteRelays(), containsInRelativeOrder(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8082")));
    }

    @Test
    void itShouldCreateRelayListEvent2() {
        Signer signer = SimpleSigner.random();

        Set<RelayUri> readWriteRelays = Sets.newHashSet(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8081"), RelayUri.parse("ws://localhost:8080"));
        Event event0 = MoreEvents.finalize(signer, Nip65.createRelayListEvent(signer.getPublicKey(), readWriteRelays));

        List<TagValue> rTag = MoreTags.findByName(event0, IndexedTag.r);
        assertThat(rTag, hasSize(2));

        assertThat(rTag.get(0).getValuesCount(), is(1));
        assertThat(rTag.get(0).getValues(0), is("ws://localhost:8080"));

        assertThat(rTag.get(1).getValuesCount(), is(1));
        assertThat(rTag.get(1).getValues(0), is("ws://localhost:8081"));

        Nip65.ReadWriteRelays relays = Nip65.findRelays(event0);
        assertThat(relays.getReadRelays(), containsInRelativeOrder(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8081")));
        assertThat(relays.getWriteRelays(), containsInRelativeOrder(RelayUri.parse("ws://localhost:8080"), RelayUri.parse("ws://localhost:8081")));
    }
}
