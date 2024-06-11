package org.tbk.nostr.example.relay;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.tbk.nostr.proto.ClosedResponse;
import org.tbk.nostr.proto.Response;
import org.tbk.nostr.template.NostrTemplate;

import java.time.Duration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = NostrRelayTestConfig.class)
@ActiveProfiles({"test", "nip42-test"})
class NostrRelayNip42Test {

    @Autowired
    private NostrTemplate nostrTemplate;

    @Test
    void itShouldReceiveAuthChallenge() {
        Response response0 = nostrTemplate.sendPlain("")
                .blockOptional(Duration.ofSeconds(5))
                .orElseThrow();
        assertThat(response0.getKindCase(), is(Response.KindCase.CLOSED));
        ClosedResponse closed0 = response0.getClosed();
        assertThat(closed0.getMessage(), startsWith("auth-required:"));
    }
}
