package org.tbk.nostr.example.agentic;

import org.junit.jupiter.api.Test;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.identity.Identity;
import org.tbk.nostr.identity.Signer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest
@ActiveProfiles("test")
class NostrAgenticExampleApplicationTest {

    @Autowired(required = false)
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private NostrClientService nostrClientService;

    @Autowired(required = false)
    private OllamaApi ollamaApi;

    @Autowired(required = false)
    private Identity nostrIdentity;

    @Autowired(required = false)
    private Signer nostrSigner;

    @Test
    void contextLoads() {
        assertThat(applicationContext, is(notNullValue()));
        assertThat(nostrClientService, is(notNullValue()));
        assertThat(ollamaApi, is(notNullValue()));
        assertThat(nostrIdentity, is(notNullValue()));
        assertThat(nostrSigner, is(notNullValue()));
    }
}
