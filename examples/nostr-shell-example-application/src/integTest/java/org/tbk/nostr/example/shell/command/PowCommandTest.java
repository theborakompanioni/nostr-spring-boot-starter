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
@ShellTest(terminalWidth = 240)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PowCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testPowInteractive() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("nostr:>");
        });

        session.write(session.writeSequence()
                .text("pow").space()
                .text("--target").space().text("4").space()
                .text("--json").space().text("\"{ \\\"kind\\\": 1, \\\"created_at\\\": 1, \\\"content\\\":\\\"GM\\\", \\\"tags\\\": [[ \\\"expiration\\\", \\\"1710368232\\\" ]] }\"")
                .carriageReturn()
                .build());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("\"id\":\"0")
                    .containsText("\"kind\":1")
                    .containsText("\"created_at\":1")
                    .containsText("\"content\":\"GM\"")
                    .containsText("\"tags\":[[")
                    .containsText("[\"expiration\",\"1710368232\"]")
                    .containsText("[\"nonce\",\"") // verify a "nonce" tag is added
                    .containsText("]]");
        });
    }
}
