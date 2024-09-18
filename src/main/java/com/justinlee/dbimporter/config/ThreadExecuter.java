package com.justinlee.dbimporter.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadExecuter {
    @Bean("hdiExecutorService")
    public ExecutorService hdiExecutorService(){
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("hdi-common-pool-%d").build();
        ExecutorService pool = new ThreadPoolExecutor(10, 20,
                20L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(10000), namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        return pool;
    }
}
