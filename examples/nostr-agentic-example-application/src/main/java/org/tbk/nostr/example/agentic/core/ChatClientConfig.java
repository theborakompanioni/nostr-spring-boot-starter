package org.tbk.nostr.example.agentic.core;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.generation.augmentation.QueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.CompressionQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Configuration
class ChatClientConfig {

    private static final PromptTemplate questionAnswerAdvisorPromptTemplate = new PromptTemplate("""
            {query}
            
            Context information is below, surrounded by ---------------------
            
            ---------------------
            {question_answer_context}
            ---------------------
            
            Given the context and provided history information and not prior knowledge,
            reply to the user comment. If the answer is not in the context, inform
            the user that you can't answer the question.
            """);

    @Bean("questionAnswerAdvisorPromptTemplate")
    PromptTemplate questionAnswerAdvisorPromptTemplate() {
        return questionAnswerAdvisorPromptTemplate;
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore,
                                                @Qualifier("questionAnswerAdvisorPromptTemplate") PromptTemplate promptTemplate) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(promptTemplate)
                .searchRequest(SearchRequest.builder().build())
                .build();
    }

    // @Bean
    VectorStoreDocumentRetriever documentRetriever(VectorStore vectorStore) {
        return VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.70)
                .build();
    }

    //@Bean
    ContextualQueryAugmenter contextualQueryAugmenter() {
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(true)
                .build();
    }

    //@Bean
    RewriteQueryTransformer rewriteQueryTransformer(ChatClient.Builder builder) {
        return RewriteQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    //@Bean
    TranslationQueryTransformer translationQueryTransformer(ChatClient.Builder builder) {
        return TranslationQueryTransformer.builder()
                .chatClientBuilder(builder)
                .targetLanguage("english")
                .build();
    }

    //@Bean
    MultiQueryExpander multiQueryExpander(ChatClient.Builder builder) {
        return MultiQueryExpander.builder()
                .chatClientBuilder(builder)
                .numberOfQueries(3)
                .build();
    }

    //@Bean
    CompressionQueryTransformer compressionQueryTransformer(ChatClient.Builder builder) {
        return CompressionQueryTransformer.builder()
                .chatClientBuilder(builder)
                .build();
    }

    //@Bean
    RetrievalAugmentationAdvisor retrievalAugmentationAdvisor(VectorStoreDocumentRetriever documentRetriever,
                                                              QueryAugmenter queryAugmenter,
                                                              List<QueryTransformer> queryTransformers) {
        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(documentRetriever)
                .queryAugmenter(queryAugmenter)
                .queryTransformers(queryTransformers)
                .build();
    }

    @Bean
    MessageWindowChatMemory messageWindowChatMemory(ChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    @Bean
    MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();
    }

    @Bean
    Consumer<ChatClient.AdvisorSpec> randomConversationIdAdvisor() {
        return new AddConversationId(() -> UUID.randomUUID().toString());
    }


    @Bean
    @Primary
    Consumer<ChatClient.AdvisorSpec> compositeAdvisorSpecConsumer(List<Consumer<ChatClient.AdvisorSpec>> advisorSpecConsumers) {
        return advisorSpec -> advisorSpecConsumers.forEach(it -> it.accept(advisorSpec));
    }

    @Bean
    @Primary
    ChatClient defaultChatClient(ChatClient.Builder builder,
                                 List<Advisor> defaultAdvisors,
                                 Consumer<ChatClient.AdvisorSpec> defaultAdvisorSpecConsumer) {
        return builder
                .defaultSystem("You are a helpful assistant.")
                .defaultAdvisors(defaultAdvisors)
                .defaultAdvisors(defaultAdvisorSpecConsumer)
                .build();
    }

    @Bean
    ChatClient unhelpfulChatClient(ChatClient.Builder builder,
                                   List<Advisor> defaultAdvisors) {
        return builder
                .defaultSystem("You are an unhelpful assistant.")
                .build();
    }

}