package org.tbk.nostr.example.shell.command;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;

@Slf4j
@ShellTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class IdentityVanityCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testIdentityVanityPrefix() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen()).containsText("nostr:>");
        });

        String npubPrefix = "z";

        session.write(session.writeSequence()
                .text("identity-vanity").space()
                .text("--npub-prefix").space().text(npubPrefix).space()
                .carriageReturn()
                .build());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("{")
                    .containsText("\"npub\" : \"npub1" + npubPrefix)
                    .containsText("\"nsec\" : \"nsec1")
                    .containsText("\"privateKey\" : \"")
                    .containsText("\"publicKey\" : \"")
                    .containsText("}");
        });
    }

    @Test
    void testIdentityVanitySuffix() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen()).containsText("nostr:>");
        });

        String npubSuffix = "z";

        session.write(session.writeSequence()
                .text("identity-vanity").space()
                .text("--npub-suffix").space().text(npubSuffix).space()
                .carriageReturn()
                .build());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("{")
                    .containsText("\"npub\" : \"npub1").containsText(npubSuffix + "\"")
                    .containsText("\"nsec\" : \"nsec1")
                    .containsText("\"privateKey\" : \"")
                    .containsText("\"publicKey\" : \"")
                    .containsText("}");
        });
    }

    @Test
    void testIdentityVanityPrefixAndSuffix() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen()).containsText("nostr:>");
        });

        String prefixAndSuffix = "z";

        session.write(session.writeSequence()
                .text("identity-vanity").space()
                .text("--npub-prefix").space().text(prefixAndSuffix).space()
                .text("--npub-suffix").space().text(prefixAndSuffix).space()
                .carriageReturn()
                .build());

        await().atMost(10 * 2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("{")
                    .containsText("\"npub\" : \"npub1" + prefixAndSuffix).containsText(prefixAndSuffix + "\"")
                    .containsText("\"nsec\" : \"nsec1")
                    .containsText("\"privateKey\" : \"")
                    .containsText("\"publicKey\" : \"")
                    .containsText("}");
        });
    }
}
