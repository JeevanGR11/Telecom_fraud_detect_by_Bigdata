package com.telecom.dto;

import java.util.List;
import java.util.Map;

public class DashboardDTOs {

    public static class DashboardStatsDTO {
        private long totalCalls;
        private long analyzedCalls;
        private long fraudDetected;
        private double fraudDetectionRate; // Percentage
        private long totalSubscribers;
        private long activeAlerts;
        private String lastUpdated;

        public long getTotalCalls() { return totalCalls; }
        public void setTotalCalls(long totalCalls) { this.totalCalls = totalCalls; }
        public long getAnalyzedCalls() { return analyzedCalls; }
        public void setAnalyzedCalls(long analyzedCalls) { this.analyzedCalls = analyzedCalls; }
        public long getFraudDetected() { return fraudDetected; }
        public void setFraudDetected(long fraudDetected) { this.fraudDetected = fraudDetected; }
        public double getFraudDetectionRate() { return fraudDetectionRate; }
        public void setFraudDetectionRate(double fraudDetectionRate) { this.fraudDetectionRate = fraudDetectionRate; }
        public long getTotalSubscribers() { return totalSubscribers; }
        public void setTotalSubscribers(long totalSubscribers) { this.totalSubscribers = totalSubscribers; }
        public long getActiveAlerts() { return activeAlerts; }
        public void setActiveAlerts(long activeAlerts) { this.activeAlerts = activeAlerts; }
        public String getLastUpdated() { return lastUpdated; }
        public void setLastUpdated(String lastUpdated) { this.lastUpdated = lastUpdated; }
    }

    public static class ChartDataDTO {
        private List<String> labels;
        private List<Number> data;

        public ChartDataDTO() {}
        public ChartDataDTO(List<String> labels, List<Number> data) {
            this.labels = labels;
            this.data = data;
        }

        public List<String> getLabels() { return labels; }
        public void setLabels(List<String> labels) { this.labels = labels; }
        public List<Number> getData() { return data; }
        public void setData(List<Number> data) { this.data = data; }
    }

    public static class AlertItemDTO {
        private String mobileNumber;
        private int totalCalls;
        private int distinctReceivers;
        private int riskScore;
        private String riskLevel;
        private String detectionDate;
        private String reasons;

        public String getMobileNumber() { return mobileNumber; }
        public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
        public int getTotalCalls() { return totalCalls; }
        public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
        public int getDistinctReceivers() { return distinctReceivers; }
        public void setDistinctReceivers(int distinctReceivers) { this.distinctReceivers = distinctReceivers; }
        public int getRiskScore() { return riskScore; }
        public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
        public String getRiskLevel() { return riskLevel; }
        public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
        public String getDetectionDate() { return detectionDate; }
        public void setDetectionDate(String detectionDate) { this.detectionDate = detectionDate; }
        public String getReasons() { return reasons; }
        public void setReasons(String reasons) { this.reasons = reasons; }
    }
}
