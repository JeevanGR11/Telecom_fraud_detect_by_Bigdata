package com.telecom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "threat_records", indexes = {
    @Index(name = "idx_threat_ip", columnList = "ip_address"),
    @Index(name = "idx_threat_level", columnList = "threat_level")
})
public class ThreatRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ip_address", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "associated_caller", length = 15)
    private String associatedCaller;

    @Column(name = "suspicious_events", nullable = false)
    private Integer suspiciousEvents = 1;

    @Column(name = "threat_level", nullable = false, length = 20)
    private String threatLevel = "MEDIUM"; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, length = 100)
    private String classification;

    @Column(name = "activity_details", columnDefinition = "TEXT")
    private String activityDetails;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt = LocalDateTime.now();

    public ThreatRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getAssociatedCaller() { return associatedCaller; }
    public void setAssociatedCaller(String associatedCaller) { this.associatedCaller = associatedCaller; }

    public Integer getSuspiciousEvents() { return suspiciousEvents; }
    public void setSuspiciousEvents(Integer suspiciousEvents) { this.suspiciousEvents = suspiciousEvents; }

    public String getThreatLevel() { return threatLevel; }
    public void setThreatLevel(String threatLevel) { this.threatLevel = threatLevel; }

    public String getClassification() { return classification; }
    public void setClassification(String classification) { this.classification = classification; }

    public String getActivityDetails() { return activityDetails; }
    public void setActivityDetails(String activityDetails) { this.activityDetails = activityDetails; }

    public LocalDateTime getDetectedAt() { return detectedAt; }
    public void setDetectedAt(LocalDateTime detectedAt) { this.detectedAt = detectedAt; }
}
