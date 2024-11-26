package org.tbk.nostr.template.e2e;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication(proxyBeanMethods = false)
@Import(NostrTemplateE2eTestConfiguration.class)
public class NostrTemplateE2eTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrTemplateE2eTestApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(args);
    }
}