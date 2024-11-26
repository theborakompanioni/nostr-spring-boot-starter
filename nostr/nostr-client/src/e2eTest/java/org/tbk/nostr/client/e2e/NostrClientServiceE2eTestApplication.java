package org.tbk.nostr.client.e2e;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Import;

@SpringBootApplication(proxyBeanMethods = false)
@Import(NostrClientServiceE2eTestConfiguration.class)
public class NostrClientServiceE2eTestApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrClientServiceE2eTestApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("test")
                .run(args);
    }
}