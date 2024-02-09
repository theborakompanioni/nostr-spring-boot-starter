package org.tbk.nostr.relay.example;

import jakarta.persistence.EntityManagerFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.time.Duration;
import java.util.concurrent.Executor;

@Slf4j
@EnableAsync
@Configuration
@RequiredArgsConstructor
class NostrRelayExampleAsyncConfig implements AsyncConfigurer {

    @NonNull
    private final NostrRelayExampleApplicationProperties properties;

    @Override
    public Executor getAsyncExecutor() {
        return asyncThreadPoolTaskExecutor();
    }

    @Bean
    ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setThreadNamePrefix("tbk-async-");
        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(properties.getAsyncExecutor().getMaxPoolSize());
        threadPoolTaskExecutor.setQueueCapacity(Integer.MAX_VALUE);
        threadPoolTaskExecutor.setDaemon(false);
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAwaitTerminationSeconds((int) Duration.ofMinutes(2).toSeconds());

        return threadPoolTaskExecutor;
    }

    /**
     * Async tasks that were submitted often need a database connection.
     * This bean should ensure that the entity manager factory is closed after the task executors shuts down.
     */
    @Bean
    DisposableBean gracefulAsyncTaskExecutorShutdown(ThreadPoolTaskExecutor asyncThreadPoolTaskExecutor,
                                                     EntityManagerFactory entityManagerFactory) {
        return () -> {
            log.info("Commencing graceful async task executor shutdown. Waiting for {} active async tasks to complete. {} tasks queued.",
                    asyncThreadPoolTaskExecutor.getActiveCount(),
                    asyncThreadPoolTaskExecutor.getQueueSize());

            if (!entityManagerFactory.isOpen()) {
                log.warn("Shutting down async task executor, but entity manager factory already closed. " +
                         "Already queued tasks that need database access will fail. ");
            }

            asyncThreadPoolTaskExecutor.shutdown();

            log.info("Graceful async task executor shutdown complete.");
        };
    }
}
