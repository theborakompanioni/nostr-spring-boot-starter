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
@ShellTest(terminalWidth = 120)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PersonaCommandTest {

    @Autowired
    private ShellTestClient client;

    @Test
    void testPersonaAlice0() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("nostr:>");
        });

        session.write(session.writeSequence()
                .text("persona").space()
                .text("--name").space().text("alice").space()
                .carriageReturn()
                .build());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("{")
                    .containsText("\"entropy\" : \"2bd806c97f0e00af1a1fc3328fa763a9\"")
                    .containsText("\"mnemonic\" : \"cloth scan rather wrap theme fiscal half wear crater large suggest fancy\"")
                    .containsText("\"privateKey\" : \"7eaab2f5e9359badb538722e23e6e65bb0c8265a707d317ec4b132ccd23aeb72\"")
                    .containsText("\"publicKey\" : \"f319269a8757e84e9b6dad9325cb74933f64e9497c4c3a8f7757361e78edf564\"")
                    .containsText("\"nsec\" : \"nsec1064t9a0fxkd6mdfcwghz8ehxtwcvsfj6wp7nzlkykyeve536adeqjksgqj\"")
                    .containsText("\"npub\" : \"npub17vvjdx582l5yaxmd4kfjtjm5jvlkf62f03xr4rmh2umpu78d74jqxhkuj6\"")
                    .containsText("}");
        });
    }

    @Test
    void testPersonaBob0() {
        ShellTestClient.InteractiveShellSession session = client
                .interactive()
                .run();

        await().atMost(2, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("nostr:>");
        });

        session.write(session.writeSequence()
                .text("persona").space()
                .text("--name").space().text("bob").space()
                .carriageReturn()
                .build());

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ShellAssertions.assertThat(session.screen())
                    .containsText("{")
                    .containsText("\"entropy\" : \"81b637d8fcd2c6da6359e6963113a117\"")
                    .containsText("\"mnemonic\" : \"like random wage whale cluster honey miracle devote normal mass tribe comfort\"")
                    .containsText("\"privateKey\" : \"db5348eb22abb023fde6015a562a56a09da6ee2458c7714cb458af8d7441e6e3\"")
                    .containsText("\"publicKey\" : \"72603b8a1329cd9cb7e117f1d9d6ae6bb9385ec591d30a3c91ef40aa8aa4c409\"")
                    .containsText("\"nsec\" : \"nsec1mdf536ez4wcz8l0xq9d9v2jk5zw6dm3ytrrhzn95tzhc6azpum3s9xd9wz\"")
                    .containsText("\"npub\" : \"npub1wfsrhzsn98xeedlpzlcan44wdwunshk9j8fs50y3aaq24z4ycsysuspqy8\"")
                    .containsText("}");
        });
    }
}
