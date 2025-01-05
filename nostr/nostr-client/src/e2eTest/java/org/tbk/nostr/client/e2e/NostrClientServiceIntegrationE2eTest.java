package org.tbk.nostr.client.e2e;

import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.base.Kinds;
import org.tbk.nostr.base.SubscriptionId;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nip19.Npub;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.Metadata;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.proto.json.JsonReader;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrClientServiceIntegrationE2eTest {
    private static final Npub fiatjaf = Nip19.decodeNpub("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6");

    @Autowired
    private NostrClientService sut;

    @Test
    void itShouldFetchMetadataEventSuccessfully0() {
        SubscriptionId subscriptionId = MoreSubscriptionIds.random();

        Event event = sut.subscribeToEvents(ReqRequest.newBuilder()
                        .setId(subscriptionId.getId())
                        .addFilters(Filter.newBuilder()
                                .addKinds(Kinds.kindProfileMetadata.getValue())
                                .addAuthors(ByteString.copyFrom(fiatjaf.getPublicKey().value.toByteArray()))
                                .build())
                        .build())
                .next()
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow();

        assertThat(event.getKind(), is(Kinds.kindProfileMetadata.getValue()));

        Metadata metadata = JsonReader.fromJson(event.getContent(), Metadata.newBuilder());
        assertThat(metadata, is(notNullValue()));
    }
}
