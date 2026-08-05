package org.tbk.nostr.example.agentic;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.example.agentic.utils.MostMinimalPrompt;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrAgenticExampleApplicationE2eTest {

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    void itShouldVerifyMinimalPrompt() {
        String contents = MostMinimalPrompt.prompt();

        OllamaChatOptions options = ollamaChatModel.getOptions();
        ChatResponse response = ollamaChatModel.call(new Prompt(contents, options));

        assertThat(response.getResult(), is(notNullValue()));

        String text = response.getResult().getOutput().getText();
        assertThat(text, is(notNullValue()));
    }
}
