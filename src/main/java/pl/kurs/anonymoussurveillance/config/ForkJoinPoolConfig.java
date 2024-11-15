package pl.kurs.anonymoussurveillance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ForkJoinPool;

@Configuration
public class ForkJoinPoolConfig {
    private final int THREADS = 32;

    @Bean
    public ForkJoinPool forkJoinPool() {
        return new ForkJoinPool(THREADS);
    }
}
