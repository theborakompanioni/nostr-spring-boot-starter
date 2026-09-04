package org.tbk.nostr.example.agentic.core;

import lombok.NonNull;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.function.Consumer;
import java.util.function.Supplier;

public record AddConversationId(
        @NonNull Supplier<String> conversationIdSupplier
) implements Consumer<ChatClient.AdvisorSpec> {

    @Override
    public void accept(ChatClient.AdvisorSpec advisorSpec) {
        String conversationId = conversationIdSupplier.get();
        if (conversationId != null) {
            advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId);
        }
    }
}
