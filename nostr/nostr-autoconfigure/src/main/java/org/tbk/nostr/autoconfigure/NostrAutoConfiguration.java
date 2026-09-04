package org.tbk.nostr.autoconfigure;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(NostrProperties.class)
@RequiredArgsConstructor
public class NostrAutoConfiguration {

    @NonNull
    private final NostrProperties nostrProperties;
}
