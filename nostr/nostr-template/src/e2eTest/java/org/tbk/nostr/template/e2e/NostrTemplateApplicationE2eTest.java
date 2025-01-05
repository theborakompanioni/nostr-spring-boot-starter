package org.tbk.nostr.template.e2e;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.nip11.RelayInfoDocument;
import org.tbk.nostr.nip19.Nip19;
import org.tbk.nostr.nip19.Npub;
import org.tbk.nostr.proto.Metadata;
import org.tbk.nostr.template.NostrTemplate;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrTemplateApplicationE2eTest {
    private static final Npub fiatjaf = Nip19.decodeNpub("npub180cvv07tjdrrgpa0j7j7tmnyl2yr6yr7l8j4s3evf6u64th6gkwsyjh6w6");

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private NostrTemplate sut;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(sut, is(notNullValue()));
    }

    @Test
    void itShouldFetchRelayInfoDocumentSuccessfully0() {
        RelayInfoDocument relayInfo = sut.fetchRelayInfoDocument()
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow();
        assertThat(relayInfo, is(notNullValue()));
    }

    @Test
    void itShouldFetchMetadataEventSuccessfully0() {
        Metadata metadata = sut.fetchMetadataByAuthor(fiatjaf.getPublicKey())
                .blockOptional(Duration.ofSeconds(10))
                .orElseThrow();

        assertThat(metadata, is(notNullValue()));
    }
}
