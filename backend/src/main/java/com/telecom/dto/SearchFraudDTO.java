package com.telecom.dto;

import java.util.List;
import java.util.Map;

public class SearchFraudDTO {

    private boolean found;
    private String message;
    private String mobileNumber;
    private int totalCalls;
    private int incomingCalls;
    private int outgoingCalls;
    private int internationalCalls;
    private int missedCalls;
    private long totalDurationSeconds;
    private double avgDurationSeconds;
    private int distinctReceiversCount;
    private int suspiciousCallsCount;
    private int nightCallsCount;
    private int riskScore; // 0 - 100
    private String riskClassification; // LOW RISK, MEDIUM RISK, HIGH RISK, NORMAL
    private List<String> alertReasons;
    private List<CallItemDTO> callHistory;
    private Map<String, Integer> hourlyDistribution;
    private Map<String, Integer> callTypeDistribution;

    public static class CallItemDTO {
        private String recordId;
        private String receiverNumber;
        private String callType;
        private int duration;
        private String timestamp;
        private String location;
        private String ipAddress;
        private boolean isSuspicious;

        public String getRecordId() { return recordId; }
        public void setRecordId(String recordId) { this.recordId = recordId; }
        public String getReceiverNumber() { return receiverNumber; }
        public void setReceiverNumber(String receiverNumber) { this.receiverNumber = receiverNumber; }
        public String getCallType() { return callType; }
        public void setCallType(String callType) { this.callType = callType; }
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = duration; }
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getIpAddress() { return ipAddress; }
        public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
        public boolean isSuspicious() { return isSuspicious; }
        public void setSuspicious(boolean suspicious) { isSuspicious = suspicious; }
    }

    public boolean isFound() { return found; }
    public void setFound(boolean found) { this.found = found; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public int getTotalCalls() { return totalCalls; }
    public void setTotalCalls(int totalCalls) { this.totalCalls = totalCalls; }
    public int getIncomingCalls() { return incomingCalls; }
    public void setIncomingCalls(int incomingCalls) { this.incomingCalls = incomingCalls; }
    public int getOutgoingCalls() { return outgoingCalls; }
    public void setOutgoingCalls(int outgoingCalls) { this.outgoingCalls = outgoingCalls; }
    public int getInternationalCalls() { return internationalCalls; }
    public void setInternationalCalls(int internationalCalls) { this.internationalCalls = internationalCalls; }
    public int getMissedCalls() { return missedCalls; }
    public void setMissedCalls(int missedCalls) { this.missedCalls = missedCalls; }
    public long getTotalDurationSeconds() { return totalDurationSeconds; }
    public void setTotalDurationSeconds(long totalDurationSeconds) { this.totalDurationSeconds = totalDurationSeconds; }
    public double getAvgDurationSeconds() { return avgDurationSeconds; }
    public void setAvgDurationSeconds(double avgDurationSeconds) { this.avgDurationSeconds = avgDurationSeconds; }
    public int getDistinctReceiversCount() { return distinctReceiversCount; }
    public void setDistinctReceiversCount(int distinctReceiversCount) { this.distinctReceiversCount = distinctReceiversCount; }
    public int getSuspiciousCallsCount() { return suspiciousCallsCount; }
    public void setSuspiciousCallsCount(int suspiciousCallsCount) { this.suspiciousCallsCount = suspiciousCallsCount; }
    public int getNightCallsCount() { return nightCallsCount; }
    public void setNightCallsCount(int nightCallsCount) { this.nightCallsCount = nightCallsCount; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public String getRiskClassification() { return riskClassification; }
    public void setRiskClassification(String riskClassification) { this.riskClassification = riskClassification; }
    public List<String> getAlertReasons() { return alertReasons; }
    public void setAlertReasons(List<String> alertReasons) { this.alertReasons = alertReasons; }
    public List<CallItemDTO> getCallHistory() { return callHistory; }
    public void setCallHistory(List<CallItemDTO> callHistory) { this.callHistory = callHistory; }
    public Map<String, Integer> getHourlyDistribution() { return hourlyDistribution; }
    public void setHourlyDistribution(Map<String, Integer> hourlyDistribution) { this.hourlyDistribution = hourlyDistribution; }
    public Map<String, Integer> getCallTypeDistribution() { return callTypeDistribution; }
    public void setCallTypeDistribution(Map<String, Integer> callTypeDistribution) { this.callTypeDistribution = callTypeDistribution; }
}
