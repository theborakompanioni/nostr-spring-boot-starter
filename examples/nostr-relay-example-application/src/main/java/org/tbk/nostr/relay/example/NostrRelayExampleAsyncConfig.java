package org.tbk.nostr.relay.example;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
class NostrRelayExampleAsyncConfig implements AsyncConfigurer {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("tbk-async-");
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(properties.getAsyncExecutor().getMaxPoolSize());
        threadPoolTaskExecutor.setQueueCapacity(Integer.MAX_VALUE);
        threadPoolTaskExecutor.setDaemon(false);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds((int) Duration.ofMinutes(2).toSeconds());

        // must call initialize otherwise @Async methods won't work!
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
