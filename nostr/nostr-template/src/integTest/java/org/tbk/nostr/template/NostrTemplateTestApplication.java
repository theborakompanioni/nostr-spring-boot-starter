package org.tbk.nostr.template;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication(proxyBeanMethods = false)
@Import(NostrTemplateTestConfiguration.class)
public class NostrTemplateTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrTemplateTestApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(args);
    }
}