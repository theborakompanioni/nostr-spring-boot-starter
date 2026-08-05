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
class IdentityCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testIdentityInteractive() throws Exception {
        ShellScreen screen = client.sendCommand("identity");

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"privateKey\" : \"")
                .containsText("\"publicKey\" : \"")
                .containsText("\"nsec\" : \"nsec1")
                .containsText("\"npub\" : \"npub1")
                .containsText("}");
    }
}
