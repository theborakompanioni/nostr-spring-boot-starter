package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.template.NostrTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip50-postgres-test"})
class NostrRelayNip50PostgresTest {

    @Autowired
    private NostrTemplate nostrTemplate;

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
