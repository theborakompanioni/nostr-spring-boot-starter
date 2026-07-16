package org.tbk.nostr.example.shell.command;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.shell.test.ShellAssertions;
import org.springframework.shell.test.ShellScreen;
import org.springframework.shell.test.ShellTestClient;
import org.springframework.shell.test.autoconfigure.ShellTest;
import org.tbk.nostr.example.shell.NostrShellExampleApplication;

@Slf4j
@ShellTest
@SpringBootTest(classes = NostrShellExampleApplication.class)
class PowCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testPowInteractive() throws Exception {
        String command = """
                pow --target 4 --json "{ \\"kind\\": 1, \\"created_at\\": 1, \\"content\\":\\"GM\\", \\"tags\\": [[ \\"expiration\\", \\"1710368232\\" ]] }"
                """;

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("\"id\":\"0")
                .containsText("\"kind\":1")
                .containsText("\"created_at\":1")
                .containsText("\"content\":\"GM\"")
                .containsText("\"tags\":[[")
                .containsText("[\"expiration\",\"1710368232\"]")
                .containsText("[\"nonce\",\"") // verify a "nonce" tag is added
                .containsText("]]");
    }
}
