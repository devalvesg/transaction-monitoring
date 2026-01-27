package com.devalvesg.transaction_service.application.config;

import com.devalvesg.transaction_service.adapters.persistence.TransactionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    @Bean
    public Gauge transactionTotalDatabaseGauge(MeterRegistry registry, TransactionRepository repository) {
        return Gauge.builder("transactions.database.total", repository, TransactionRepository::count)
                .description("Total transactions in database")
                .register(registry);
    }

    @Bean
    public Counter transactionCreatedCounter(MeterRegistry registry) {
        return Counter.builder("transactions.created.total")
                .description("Total transactions created")
                .register(registry);
    }

    @Bean
    public Timer transactionCreationTimer(MeterRegistry registry) {
        return Timer.builder("transactions.creation.duration")
                .description("Transaction creation time")
                .register(registry);
    }

    @Bean
    public Counter fraudAlertReceivedCounter(MeterRegistry registry) {
        return Counter.builder("fraud.alerts.received.total")
                .description("Total fraud alerts received")
                .register(registry);
    }

    @Bean
    public Counter fraudDetectedCounter(MeterRegistry registry) {
        return Counter.builder("fraud.detected.total")
                .description("Total transactions flagged as fraud")
                .register(registry);
    }

    @Bean
    public DistributionSummary transactionAmountSummary(MeterRegistry registry) {
        return DistributionSummary.builder("transactions.amount.summary")
                .description("Transaction amounts")
                .baseUnit("currency_units")
                .register(registry);
    }

    @Bean
    public DistributionSummary riskScoreSummary(MeterRegistry registry) {
        return DistributionSummary.builder("transactions.risk.score.summary")
                .description("Risk scores")
                .baseUnit("points")
                .register(registry);
    }

    @Bean
    public AtomicInteger pendingTransactionCount() {
        return new AtomicInteger(0);
    }

    @Bean
    public Gauge pendingTransactionGauge(MeterRegistry registry, AtomicInteger pendingTransactionCount) {
        return Gauge.builder("transactions.pending.count", pendingTransactionCount, AtomicInteger::get)
                .description("Current pending transactions")
                .register(registry);
    }
}
