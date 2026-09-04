package org.tbk.nostr.example.agentic.core;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.embedding.observation.EmbeddingModelObservationConvention;
import org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingProperties;
import org.springframework.ai.model.ollama.autoconfigure.OllamaInitializationProperties;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaEmbeddingOptions;
import org.springframework.ai.ollama.management.ModelManagementOptions;
import org.springframework.ai.ollama.management.PullModelStrategy;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;

public class OllamaEmbeddingConfiguration {

    /**
     * copied from
     * {@link org.springframework.ai.model.ollama.autoconfigure.OllamaEmbeddingAutoConfiguration},
     * but with support for setting dimensions
     */
    @Bean
    public OllamaEmbeddingModel ollamaEmbeddingModel(OllamaApi ollamaApi, OllamaEmbeddingProperties properties,
                                                     OllamaInitializationProperties initProperties, ObjectProvider<ObservationRegistry> observationRegistry,
                                                     ObjectProvider<EmbeddingModelObservationConvention> observationConvention) {
        var embeddingModelPullStrategy = initProperties.getEmbedding().isInclude()
                ? initProperties.getPullModelStrategy() : PullModelStrategy.NEVER;

        OllamaEmbeddingOptions options = properties.toOptions();

        var embeddingModel = OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .options(options)
                .observationRegistry(observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP))
                .modelManagementOptions(new ModelManagementOptions(embeddingModelPullStrategy,
                        initProperties.getEmbedding().getAdditionalModels(), initProperties.getTimeout(),
                        initProperties.getMaxRetries()))
                .build();

        observationConvention.ifAvailable(embeddingModel::setObservationConvention);

        return embeddingModel;
    }
}
