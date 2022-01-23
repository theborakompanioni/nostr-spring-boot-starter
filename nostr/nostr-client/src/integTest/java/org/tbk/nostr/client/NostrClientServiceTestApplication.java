package org.tbk.nostr.client;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication(proxyBeanMethods = false)
@Import(NostrClientServiceTestConfiguration.class)
public class NostrClientServiceTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrClientServiceTestApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(args);
    }
}