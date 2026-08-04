package org.tbk.nostr.example.agentic.utils;

public final class MostMinimalPrompt {
    // If you want to test the absolute floor of the model's logic without
    // asking it to generate a creative sentence, you can use a simple
    // identity prompt: "1+1". This forces the model to retrieve a factual
    // constant rather than synthesizing a conversational response.
    private static final String prompt = "1+1";

    public static String prompt() {
        return prompt;
    }

    private MostMinimalPrompt() {
        throw new UnsupportedOperationException();
    }
}
