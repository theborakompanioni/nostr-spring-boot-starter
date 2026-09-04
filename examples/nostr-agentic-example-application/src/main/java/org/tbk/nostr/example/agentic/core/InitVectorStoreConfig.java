package org.tbk.nostr.example.agentic.core;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.tbk.nostr.example.agentic.NostrAgenticExampleApplicationProperties;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
class InitVectorStoreConfig {

    @NonNull
    private final NostrAgenticExampleApplicationProperties properties;

    @Bean
    ApplicationRunner readDocs(VectorStore vectorStore) {
        Resource document = properties.getDocument();
        if (document == null) {
            log.debug("Vector Store init: No document has been provided. Skipping.");
            return args -> {
            };
        }

        TextSplitter splitter = TokenTextSplitter.builder().build();
        return args -> {
            log.debug("Vector Store init: Init with {}", document.getFilename());
            //documents.forEach(it -> {
            TikaDocumentReader documentReader = new TikaDocumentReader(document);
            List<Document> docs = splitter.apply(documentReader.read());
            vectorStore.add(docs);
            //});
        };
    }
}