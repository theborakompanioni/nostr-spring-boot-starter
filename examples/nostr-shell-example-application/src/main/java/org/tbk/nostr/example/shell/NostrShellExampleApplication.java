package org.tbk.nostr.example.shell;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.ApplicationListener;

import java.util.Locale;
import java.util.TimeZone;

@SpringBootApplication(proxyBeanMethods = false)
public class NostrShellExampleApplication {
    static {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Locale.setDefault(Locale.ENGLISH);
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder()
                .sources(NostrShellExampleApplication.class)
                .listeners(applicationPidFileWriter())
                .web(WebApplicationType.NONE)
                .run(args);
    }

    private static ApplicationListener<?> applicationPidFileWriter() {
        return new ApplicationPidFileWriter("application.pid");
    }
}
