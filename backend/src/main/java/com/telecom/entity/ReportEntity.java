package com.telecom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
public class ReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "generated_by", nullable = false, length = 50)
    private String generatedBy;

    @Column(name = "report_type", nullable = false, length = 50)
    private String reportType = "FULL_FRAUD_AUDIT";

    @Column(name = "records_analyzed")
    private Integer recordsAnalyzed = 0;

    @Column(name = "fraud_cases_found")
    private Integer fraudCasesFound = 0;

    @Column(name = "high_risk_count")
    private Integer highRiskCount = 0;

    @Column(name = "medium_risk_count")
    private Integer mediumRiskCount = 0;

    @Column(name = "low_risk_count")
    private Integer lowRiskCount = 0;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public ReportEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getGeneratedBy() { return generatedBy; }
    public void setGeneratedBy(String generatedBy) { this.generatedBy = generatedBy; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }

    public Integer getRecordsAnalyzed() { return recordsAnalyzed; }
    public void setRecordsAnalyzed(Integer recordsAnalyzed) { this.recordsAnalyzed = recordsAnalyzed; }

    public Integer getFraudCasesFound() { return fraudCasesFound; }
    public void setFraudCasesFound(Integer fraudCasesFound) { this.fraudCasesFound = fraudCasesFound; }

    public Integer getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(Integer highRiskCount) { this.highRiskCount = highRiskCount; }

    public Integer getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(Integer mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public Integer getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(Integer lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
