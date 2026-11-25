package com.telecom.dto;

import java.util.List;
import java.util.Map;

public class ClusteringDTOs {

    public static class ClusterGroupDTO {
        private int clusterId;
        private String clusterName;
        private int memberCount;
        private double avgFrequency;
        private double avgDuration;
        private double avgDistinctReceivers;
        private double avgNightRatio;
        private double avgRiskScore;
        private boolean isAnomalous;
        private String description;
        private List<String> sampleMobiles;

        public int getClusterId() { return clusterId; }
        public void setClusterId(int clusterId) { this.clusterId = clusterId; }
        public String getClusterName() { return clusterName; }
        public void setClusterName(String clusterName) { this.clusterName = clusterName; }
        public int getMemberCount() { return memberCount; }
        public void setMemberCount(int memberCount) { this.memberCount = memberCount; }
        public double getAvgFrequency() { return avgFrequency; }
        public void setAvgFrequency(double avgFrequency) { this.avgFrequency = avgFrequency; }
        public double getAvgDuration() { return avgDuration; }
        public void setAvgDuration(double avgDuration) { this.avgDuration = avgDuration; }
        public double getAvgDistinctReceivers() { return avgDistinctReceivers; }
        public void setAvgDistinctReceivers(double avgDistinctReceivers) { this.avgDistinctReceivers = avgDistinctReceivers; }
        public double getAvgNightRatio() { return avgNightRatio; }
        public void setAvgNightRatio(double avgNightRatio) { this.avgNightRatio = avgNightRatio; }
        public double getAvgRiskScore() { return avgRiskScore; }
        public void setAvgRiskScore(double avgRiskScore) { this.avgRiskScore = avgRiskScore; }
        public boolean isAnomalous() { return isAnomalous; }
        public void setAnomalous(boolean anomalous) { isAnomalous = anomalous; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public List<String> getSampleMobiles() { return sampleMobiles; }
        public void setSampleMobiles(List<String> sampleMobiles) { this.sampleMobiles = sampleMobiles; }
    }
}
