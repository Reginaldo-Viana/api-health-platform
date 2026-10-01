package com.apihealth.platform.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "daily_reports", uniqueConstraints = @UniqueConstraint(columnNames = "report_date"))
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private long totalChecks;

    @Column(nullable = false)
    private long successfulChecks;

    @Column(nullable = false)
    private long failedChecks;

    @Column(nullable = false)
    private double availabilityPercentage;

    @Column(nullable = false)
    private double averageLatencyMs;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    protected DailyReport() {
    }

    public DailyReport(LocalDate reportDate, long totalChecks, long successfulChecks, long failedChecks,
                       double availabilityPercentage, double averageLatencyMs, LocalDateTime generatedAt) {
        this.reportDate = reportDate;
        this.totalChecks = totalChecks;
        this.successfulChecks = successfulChecks;
        this.failedChecks = failedChecks;
        this.availabilityPercentage = availabilityPercentage;
        this.averageLatencyMs = averageLatencyMs;
        this.generatedAt = generatedAt;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public long getTotalChecks() {
        return totalChecks;
    }

    public long getSuccessfulChecks() {
        return successfulChecks;
    }

    public long getFailedChecks() {
        return failedChecks;
    }

    public double getAvailabilityPercentage() {
        return availabilityPercentage;
    }

    public double getAverageLatencyMs() {
        return averageLatencyMs;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void updateTotals(long totalChecks, long successfulChecks, long failedChecks,
                             double availabilityPercentage, double averageLatencyMs,
                             LocalDateTime generatedAt) {
        this.totalChecks = totalChecks;
        this.successfulChecks = successfulChecks;
        this.failedChecks = failedChecks;
        this.availabilityPercentage = availabilityPercentage;
        this.averageLatencyMs = averageLatencyMs;
        this.generatedAt = generatedAt;
    }
}