package org.tbk.nostr.example.agentic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ollama.autoconfigure.OllamaChatProperties;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.server.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.util.StopWatch;
import org.tbk.nostr.client.NostrClientService;
import org.tbk.nostr.example.agentic.utils.MostMinimalPrompt;
import org.tbk.nostr.identity.Identity;

import java.util.Locale;
import java.util.TimeZone;

@Slf4j
@SpringBootApplication(proxyBeanMethods = false)
public class NostrAgenticExampleApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrAgenticExampleApplication.class)
                .listeners(applicationPidFileWriter(), webServerPortFileWriter())
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    private static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }

    private static ApplicationListener<?> webServerPortFileWriter() {
        return new WebServerPortFileWriter("application.port");
    }

    //@Bean
    MainApplicationRunner mainApplicationRunner(NostrClientService nostrClientService) {
        return new MainApplicationRunner(nostrClientService);
    }

    @Bean
    ApplicationRunner appInfoLogger(Identity nostrIdentity,
                                    OllamaChatProperties ollamaChatProperties) {
        return args -> {
            log.info("Identity Public Key (#0): {}", nostrIdentity.deriveAccount(0).getPublicKey().value.toHex());
            log.info("Default Chat Model: {}", ollamaChatProperties.getModel());
        };
    }

    @Bean
    @Profile("!test")
    ApplicationRunner testRunner(OllamaChatModel ollamaChatModel) {
        return args -> {
            MostMinimalPrompt prompt = MostMinimalPrompt.create();

            OllamaChatOptions options = ollamaChatModel.getOptions().mutate()
                    .combineWith(prompt.getOptions().mutate())
                    .disableThinking()
                    .build();

            StopWatch stopWatch = new StopWatch("chatModel.call");

            stopWatch.start();
            ChatResponse response = ollamaChatModel.call(new Prompt(prompt.getPrompt(), options));
            stopWatch.stop();

            log.info("""
                    \n***** TEST PROMPT *****
                    * prompt: "{}"
                    * response: "{}"
                    * =====================
                    * model: {}
                    * options: {}
                    * stopwatch: {}
                    ***********************
                    """,
                    prompt.getPrompt(),
                    response.getResult().getOutput().getText(),
                    response.getMetadata().getModel(),
                    options.toMap(),
                    stopWatch.shortSummary());
        };
    }
}
