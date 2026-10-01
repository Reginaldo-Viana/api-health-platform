package com.apihealth.platform.model;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "check_results")
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false)
    private String targetName;

    @Column(nullable = false, length = 1000)
    private String targetUrl;

    @Column(nullable = false)
    private LocalDateTime checkedAt;

    @Column(nullable = false)
    private long latencyMs;

    private Integer statusCode;

    @Column(nullable = false)
    private boolean available;

    @Column(length = 500)
    private String errorMessage;

    protected CheckResult() {
    }

    public CheckResult(Long targetId, String targetName, String targetUrl, LocalDateTime checkedAt,
                       long latencyMs, Integer statusCode, boolean available, String errorMessage) {
        this.targetId = targetId;
        this.targetName = targetName;
        this.targetUrl = targetUrl;
        this.checkedAt = checkedAt;
        this.latencyMs = latencyMs;
        this.statusCode = statusCode;
        this.available = available;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public Long getTargetId() {
        return targetId;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public LocalDateTime getCheckedAt() {
        return checkedAt;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}