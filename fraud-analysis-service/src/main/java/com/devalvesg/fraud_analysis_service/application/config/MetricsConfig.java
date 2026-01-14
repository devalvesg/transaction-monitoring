package com.devalvesg.fraud_analysis_service.application.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    @Bean
    public Map<String, Long> blacklistedAddressesMap() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Counter fraudAnalysisCompletedCounter(MeterRegistry registry) {
        return Counter.builder("fraud.analysis.completed.total")
                .description("Total fraud analyses completed")
                .register(registry);
    }

    @Bean
    public Counter fraudDetectedCounter(MeterRegistry registry) {
        return Counter.builder("fraud.analysis.fraud_detected.total")
                .description("Total transactions flagged as fraud")
                .register(registry);
    }

    @Bean
    public Counter cleanTransactionsCounter(MeterRegistry registry) {
        return Counter.builder("fraud.analysis.clean.total")
                .description("Total clean transactions")
                .register(registry);
    }

    @Bean
    public Timer fraudAnalysisTimer(MeterRegistry registry) {
        return Timer.builder("fraud.analysis.duration")
                .description("Fraud analysis duration")
                .register(registry);
    }

    @Bean
    public DistributionSummary riskScoreDistribution(MeterRegistry registry) {
        return DistributionSummary.builder("fraud.analysis.risk_score.distribution")
                .description("Risk score distribution")
                .baseUnit("points")
                .publishPercentiles(0.5, 0.75, 0.95, 0.99)
                .register(registry);
    }

    @Bean
    public Counter blacklistAddCounter(MeterRegistry registry) {
        return Counter.builder("blacklist.addresses.added.total")
                .description("Addresses added to blacklist")
                .register(registry);
    }

    @Bean
    public AtomicInteger blacklistSizeHolder() {
        return new AtomicInteger(0);
    }

    @Bean
    public Gauge blacklistSizeGauge(MeterRegistry registry, AtomicInteger blacklistSizeHolder) {
        return Gauge.builder("blacklist.size.current", blacklistSizeHolder, AtomicInteger::get)
                .description("Current blacklist size")
                .register(registry);
    }
}
