package com.telecom.dto;

import java.util.Map;

public class HadoopDTOs {

    public static class HadoopStatusDTO {
        private boolean hadoopConnected;
        private boolean hdfsConnected;
        private String nameNodeUrl;
        private String clusterStatus;
        private String hdfsInputPath;
        private String hdfsOutputPath;
        private long totalHdfsFiles;
        private String activeJobStatus;
        private String lastExecutionTime;
        private Map<String, Object> connectionDetails;

        public boolean isHadoopConnected() { return hadoopConnected; }
        public void setHadoopConnected(boolean hadoopConnected) { this.hadoopConnected = hadoopConnected; }
        public boolean isHdfsConnected() { return hdfsConnected; }
        public void setHdfsConnected(boolean hdfsConnected) { this.hdfsConnected = hdfsConnected; }
        public String getNameNodeUrl() { return nameNodeUrl; }
        public void setNameNodeUrl(String nameNodeUrl) { this.nameNodeUrl = nameNodeUrl; }
        public String getClusterStatus() { return clusterStatus; }
        public void setClusterStatus(String clusterStatus) { this.clusterStatus = clusterStatus; }
        public String getHdfsInputPath() { return hdfsInputPath; }
        public void setHdfsInputPath(String hdfsInputPath) { this.hdfsInputPath = hdfsInputPath; }
        public String getHdfsOutputPath() { return hdfsOutputPath; }
        public void setHdfsOutputPath(String hdfsOutputPath) { this.hdfsOutputPath = hdfsOutputPath; }
        public long getTotalHdfsFiles() { return totalHdfsFiles; }
        public void setTotalHdfsFiles(long totalHdfsFiles) { this.totalHdfsFiles = totalHdfsFiles; }
        public String getActiveJobStatus() { return activeJobStatus; }
        public void setActiveJobStatus(String activeJobStatus) { this.activeJobStatus = activeJobStatus; }
        public String getLastExecutionTime() { return lastExecutionTime; }
        public void setLastExecutionTime(String lastExecutionTime) { this.lastExecutionTime = lastExecutionTime; }
        public Map<String, Object> getConnectionDetails() { return connectionDetails; }
        public void setConnectionDetails(Map<String, Object> connectionDetails) { this.connectionDetails = connectionDetails; }
    }

    public static class JobTriggerRequest {
        private String datasetName;
        private String inputPath;
        private int frequencyThreshold = 8;
        private int distinctReceiversThreshold = 6;
        private int wangiriDurationThreshold = 5;
        private int excessiveDurationThreshold = 3600;
        private double nightRatioThreshold = 0.35;

        public String getDatasetName() { return datasetName; }
        public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
        public String getInputPath() { return inputPath; }
        public void setInputPath(String inputPath) { this.inputPath = inputPath; }
        public int getFrequencyThreshold() { return frequencyThreshold; }
        public void setFrequencyThreshold(int frequencyThreshold) { this.frequencyThreshold = frequencyThreshold; }
        public int getDistinctReceiversThreshold() { return distinctReceiversThreshold; }
        public void setDistinctReceiversThreshold(int distinctReceiversThreshold) { this.distinctReceiversThreshold = distinctReceiversThreshold; }
        public int getWangiriDurationThreshold() { return wangiriDurationThreshold; }
        public void setWangiriDurationThreshold(int wangiriDurationThreshold) { this.wangiriDurationThreshold = wangiriDurationThreshold; }
        public int getExcessiveDurationThreshold() { return excessiveDurationThreshold; }
        public void setExcessiveDurationThreshold(int excessiveDurationThreshold) { this.excessiveDurationThreshold = excessiveDurationThreshold; }
        public double getNightRatioThreshold() { return nightRatioThreshold; }
        public void setNightRatioThreshold(double nightRatioThreshold) { this.nightRatioThreshold = nightRatioThreshold; }
    }

    public static class JobStatusDTO {
        private String jobId;
        private String datasetName;
        private String status; // QUEUED, RUNNING, COMPLETED, FAILED
        private int inputRecords;
        private int processedRecords;
        private int fraudRecords;
        private long durationSeconds;
        private String startTime;
        private String endTime;
        private String errorDetails;
        private Map<String, Long> counters;

        public String getJobId() { return jobId; }
        public void setJobId(String jobId) { this.jobId = jobId; }
        public String getDatasetName() { return datasetName; }
        public void setDatasetName(String datasetName) { this.datasetName = datasetName; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public int getInputRecords() { return inputRecords; }
        public void setInputRecords(int inputRecords) { this.inputRecords = inputRecords; }
        public int getProcessedRecords() { return processedRecords; }
        public void setProcessedRecords(int processedRecords) { this.processedRecords = processedRecords; }
        public int getFraudRecords() { return fraudRecords; }
        public void setFraudRecords(int fraudRecords) { this.fraudRecords = fraudRecords; }
        public long getDurationSeconds() { return durationSeconds; }
        public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getEndTime() { return endTime; }
        public void setEndTime(String endTime) { this.endTime = endTime; }
        public String getErrorDetails() { return errorDetails; }
        public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
        public Map<String, Long> getCounters() { return counters; }
        public void setCounters(Map<String, Long> counters) { this.counters = counters; }
    }
}
