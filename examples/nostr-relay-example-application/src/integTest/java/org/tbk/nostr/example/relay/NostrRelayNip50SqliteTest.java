package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.base.EventId;
import org.tbk.nostr.identity.Signer;
import org.tbk.nostr.identity.SimpleSigner;
import org.tbk.nostr.proto.Event;
import org.tbk.nostr.proto.Filter;
import org.tbk.nostr.proto.OkResponse;
import org.tbk.nostr.proto.ReqRequest;
import org.tbk.nostr.template.NostrTemplate;
import org.tbk.nostr.util.MoreEvents;
import org.tbk.nostr.util.MoreSubscriptionIds;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip50-sqlite-test"})
class NostrRelayNip50SqliteTest {

    @Autowired
    private NostrRelayExampleApplicationProperties applicationProperties;

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldLoadProperties() {
        assertThat(applicationProperties.getAsyncExecutor().getMaxPoolSize(), is(1));
    }

    @Test
    void itShouldSearchForEventSuccessfully0() {
        NostrRelayNip50TestBase.itShouldSearchForEventSuccessfully0(nostrTemplate);
    }

    @Test
    void itShouldSearchForEventSuccessfully1() {
        NostrRelayNip50TestBase.itShouldSearchForEventSuccessfully1(nostrTemplate);
    }

    @Test
    void itShouldSearchForEventSuccessfully2Stemming() {
        NostrRelayNip50TestBase.itShouldSearchForEventSuccessfully2Stemming(nostrTemplate);
    }
}
