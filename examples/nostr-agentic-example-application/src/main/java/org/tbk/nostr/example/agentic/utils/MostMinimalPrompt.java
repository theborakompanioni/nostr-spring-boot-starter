package org.tbk.nostr.example.agentic.utils;

import lombok.*;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

@Value
@Builder(access = AccessLevel.PRIVATE)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MostMinimalPrompt {
    // If you want to test the absolute floor of the model's logic without
    // asking it to generate a creative sentence, you can use a simple
    // identity prompt: "1+1". This forces the model to retrieve a factual
    // constant rather than synthesizing a conversational response.
    private static final String DEFAULT_PROMPT = "1+1";

    // Lower values (0.0-0.3): More deterministic, focused responses. Better
    // for factual questions, classification for tasks where consistency is critical.
    // see https://docs.spring.io/spring-ai/reference/api/chat/prompt-engineering-patterns.html#_temperature
    private static final double DEFAULT_TEMPERATURE = 0.01d;

    // Low values (1-25): For single words, short phrases, or classification labels.
    // see https://docs.spring.io/spring-ai/reference/api/chat/prompt-engineering-patterns.html#_output_length_maxtokens
    private static final int DEFAULT_MAX_TOKENS = 3;

    public static MostMinimalPrompt create() {
        return MostMinimalPrompt.builder()
                .prompt(DEFAULT_PROMPT)
                .options(ChatOptions.builder()
                        .temperature(DEFAULT_TEMPERATURE)
                        .maxTokens(DEFAULT_MAX_TOKENS)
                        .topK(1)
                        .topP(1d)
                        .build())
                .build();
    }

    @NonNull
    String prompt;

    @NonNull
    @ToString.Exclude
    ChatOptions options;

    public Prompt toPrompt() {
        return new Prompt(this.prompt, this.options);
    }
}
