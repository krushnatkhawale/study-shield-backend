package com.studyshield.studyshield.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Dedicated single-thread executor for on-miss catalog seeding (TD-1). One thread keeps
 * seeds serialized and leaves a pooled DB connection for request traffic (Hikari
 * {@code maximum-pool-size} is 2 in production).
 */
@Configuration
public class CatalogSeedConfig {

    public static final String CATALOG_SEED_EXECUTOR = "catalogSeedTaskExecutor";

    @Bean(name = CATALOG_SEED_EXECUTOR)
    public ThreadPoolTaskExecutor catalogSeedTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setThreadNamePrefix("catalog-seed-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
