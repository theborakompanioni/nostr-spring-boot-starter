package org.tbk.nostr.example.agentic.core;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

public interface RagService {

    ChatResponse call(Prompt prompt);
}