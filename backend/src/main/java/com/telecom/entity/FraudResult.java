package com.telecom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_results", indexes = {
    @Index(name = "idx_fraud_mobile", columnList = "mobile_number"),
    @Index(name = "idx_fraud_risk_score", columnList = "risk_score"),
    @Index(name = "idx_fraud_risk_level", columnList = "risk_level")
})
public class FraudResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 15)
    private String mobileNumber;

    @Column(name = "total_calls", nullable = false)
    private Integer totalCalls = 0;

    @Column(name = "distinct_receivers", nullable = false)
    private Integer distinctReceivers = 0;

    @Column(name = "total_duration", nullable = false)
    private Long totalDuration = 0L;

    @Column(name = "avg_duration", nullable = false)
    private Double avgDuration = 0.0;

    @Column(name = "international_calls", nullable = false)
    private Integer internationalCalls = 0;

    @Column(name = "night_calls", nullable = false)
    private Integer nightCalls = 0;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore = 0; // 0 - 100

    @Column(name = "risk_level", nullable = false, length = 20)
    private String riskLevel = "LOW RISK"; // LOW RISK, MEDIUM RISK, HIGH RISK, NORMAL

    @Column(name = "risk_reasons", columnDefinition = "TEXT")
    private String riskReasons;

    @Column(name = "last_job_id", length = 50)
    private String lastJobId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public FraudResult() {}

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }

    public Integer getTotalCalls() { return totalCalls; }
    public void setTotalCalls(Integer totalCalls) { this.totalCalls = totalCalls; }

    public Integer getDistinctReceivers() { return distinctReceivers; }
    public void setDistinctReceivers(Integer distinctReceivers) { this.distinctReceivers = distinctReceivers; }

    public Long getTotalDuration() { return totalDuration; }
    public void setTotalDuration(Long totalDuration) { this.totalDuration = totalDuration; }

    public Double getAvgDuration() { return avgDuration; }
    public void setAvgDuration(Double avgDuration) { this.avgDuration = avgDuration; }

    public Integer getInternationalCalls() { return internationalCalls; }
    public void setInternationalCalls(Integer internationalCalls) { this.internationalCalls = internationalCalls; }

    public Integer getNightCalls() { return nightCalls; }
    public void setNightCalls(Integer nightCalls) { this.nightCalls = nightCalls; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public String getRiskReasons() { return riskReasons; }
    public void setRiskReasons(String riskReasons) { this.riskReasons = riskReasons; }

    public String getLastJobId() { return lastJobId; }
    public void setLastJobId(String lastJobId) { this.lastJobId = lastJobId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
