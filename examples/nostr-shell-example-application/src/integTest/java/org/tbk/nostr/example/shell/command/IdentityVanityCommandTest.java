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
class IdentityVanityCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testIdentityVanityPrefix() throws Exception {
        String npubPrefix = "z";
        String command = """
                identity-vanity --npub-prefix %s
                """.formatted(npubPrefix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"npub\" : \"npub1" + npubPrefix)
                .containsText("\"nsec\" : \"nsec1")
                .containsText("\"privateKey\" : \"")
                .containsText("\"publicKey\" : \"")
                .containsText("}");
    }

    @Test
    void testIdentityVanitySuffix() throws Exception {
        String npubSuffix = "z";
        String command = """
                identity-vanity --npub-suffix %s
                """.formatted(npubSuffix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"npub\" : \"npub1").containsText(npubSuffix + "\"")
                .containsText("\"nsec\" : \"nsec1")
                .containsText("\"privateKey\" : \"")
                .containsText("\"publicKey\" : \"")
                .containsText("}");
    }

    @Test
    void testIdentityVanityPrefixAndSuffix() throws Exception {
        String prefixAndSuffix = "z";
        String command = """
                identity-vanity --npub-prefix %s --npub-suffix %s
                """.formatted(prefixAndSuffix, prefixAndSuffix);

        ShellScreen screen = client.sendCommand(command);

        ShellAssertions.assertThat(screen)
                .containsText("{")
                .containsText("\"npub\" : \"npub1" + prefixAndSuffix).containsText(prefixAndSuffix + "\"")
                .containsText("\"nsec\" : \"nsec1")
                .containsText("\"privateKey\" : \"")
                .containsText("\"publicKey\" : \"")
                .containsText("}");
    }
}
