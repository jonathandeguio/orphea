package io.orphea.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@EnableAsync
@Configuration
public class AsyncConfig {
    @Bean
    public Executor threadPoolTaskExecutor() {
        return new ConcurrentTaskExecutor(Executors.newCachedThreadPool());
    }
}