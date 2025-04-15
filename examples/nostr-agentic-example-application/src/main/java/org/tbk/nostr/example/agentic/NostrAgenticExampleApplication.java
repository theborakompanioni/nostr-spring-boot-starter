package org.tbk.nostr.example.agentic;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.web.context.WebServerPortFileWriter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.tbk.nostr.client.NostrClientService;

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
    ApplicationRunner testRunner(OllamaChatModel ollamaChatModel) {
        return args -> {
            String contents = """
                    What day is today?
                    """;

            OllamaOptions build = OllamaOptions.builder()
                    .temperature(0.33)
                    .build();

            ChatResponse response = ollamaChatModel.call(new Prompt(contents, build));

            log.debug("ChatResponse: {}", response);
            log.info("Model: {}", response.getMetadata().getModel());
            log.info("Text: {}", response.getResult().getOutput().getText());
        };
    }
}
