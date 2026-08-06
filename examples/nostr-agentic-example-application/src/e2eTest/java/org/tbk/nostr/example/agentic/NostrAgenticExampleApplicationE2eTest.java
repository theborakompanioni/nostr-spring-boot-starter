package org.tbk.nostr.example.agentic;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.example.agentic.utils.MostMinimalPrompt;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class NostrAgenticExampleApplicationE2eTest {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    @Test
    void itShouldVerifyMinimalPrompt() {
        Prompt minimalPrompt = MostMinimalPrompt.create().toPrompt();

        ChatClientResponse response = chatClient.prompt(minimalPrompt)
                .call()
                .chatClientResponse();

        ChatResponse chatResponse = response.chatResponse();
        assertThat(chatResponse, is(notNullValue()));
        assertThat(chatResponse.getResult(), is(notNullValue()));
        assertThat(chatResponse.getMetadata(), is(notNullValue()));

        String modelAnswerText = chatResponse.getResult().getOutput().getText();
        assertThat(modelAnswerText, is(notNullValue()));
    }

    @Test
    void itShouldEvaluateMinimalPrompt() {
        Prompt minimalPrompt = MostMinimalPrompt.create().toPrompt();
        String userPrompt = requireNonNull(minimalPrompt.getUserMessage().getText());

        ChatResponse chatResponse = chatClient.prompt(minimalPrompt)
                .call()
                .chatResponse();

        assertThat(chatResponse, is(notNullValue()));
        assertThat(chatResponse.getResult(), is(notNullValue()));

        String modelAnswerText = chatResponse.getResult().getOutput().getText();
        assertThat(modelAnswerText, is(notNullValue()));

        // the retrieved context from the RAG flow
        List<Document> ragDocumentContext = requireNonNullElse(
                chatResponse.getMetadata().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT),
                Collections.emptyList()
        );
        EvaluationRequest evaluationRequest = new EvaluationRequest(
                userPrompt,
                ragDocumentContext,
                modelAnswerText
        );

        RelevancyEvaluator evaluator = new RelevancyEvaluator(ChatClient.builder(ollamaChatModel)
                .defaultOptions(ChatOptions.builder()
                        .model(ollamaChatModel.getOptions().getModel())
                ));

        EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);
        assertThat(evaluationResponse, is(notNullValue()));

        // e2e tests are done with small models: they might not provide good answers
        assertThat(evaluationResponse.isPass(), either(is(true)).or(is(false)));
    }
}
