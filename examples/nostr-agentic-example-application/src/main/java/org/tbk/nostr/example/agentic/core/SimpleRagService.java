package org.tbk.nostr.example.agentic.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStoreRetriever;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimpleRagService implements RagService {

    @NonNull
    private final VectorStoreRetriever retriever;

    @NonNull
    private final ChatClient chatClient;

    public ChatResponse call(Prompt prompt) {
        // Retrieve relevant documents
        List<Document> relevantDocs = retriever.similaritySearch(prompt.getContents());

        // Extract content from documents to use as context
        String context = relevantDocs.stream()
                .map(Document::getFormattedContent)
                .collect(Collectors.joining("\n\n"));

        // Generate response using the retrieved context
        String content = "Context information:\n" + context + "\n\n"
                + "User query: " + prompt.getContents();
        Prompt newPrompt = prompt.mutate()
                .content(content)
                .build();
        return chatClient.prompt(newPrompt)
                .call()
                .chatResponse();
    }
}