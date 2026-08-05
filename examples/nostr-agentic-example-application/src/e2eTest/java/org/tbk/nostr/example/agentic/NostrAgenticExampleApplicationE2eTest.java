package org.tbk.nostr.example.agentic;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
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
    private ChatClient chatClient;

    @Test
    void itShouldVerifyMinimalPrompt() {
        Prompt minimalPrompt = MostMinimalPrompt.create().toPrompt();

        ChatClientResponse response = chatClient.prompt(minimalPrompt)
                .call()
                .chatClientResponse();

        ChatResponse chatResponse = response.chatResponse();
        assertThat(chatResponse, is(notNullValue()));
        assertThat(chatResponse.getResult(), is(notNullValue()));

        String text = chatResponse.getResult().getOutput().getText();
        assertThat(text, is(notNullValue()));
    }
}
