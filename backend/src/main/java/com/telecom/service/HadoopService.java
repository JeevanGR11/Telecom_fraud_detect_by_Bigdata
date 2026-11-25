package com.telecom.service;

import com.telecom.dto.HadoopDTOs.HadoopStatusDTO;
import com.telecom.dto.HadoopDTOs.JobStatusDTO;
import com.telecom.dto.HadoopDTOs.JobTriggerRequest;
import com.telecom.entity.AnalysisJob;
import com.telecom.entity.CallRecord;
import com.telecom.entity.DatasetEntity;
import com.telecom.entity.FraudResult;
import com.telecom.repository.AnalysisJobRepository;
import com.telecom.repository.CallRecordRepository;
import com.telecom.repository.DatasetRepository;
import com.telecom.repository.FraudResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class HadoopService {

    private static final Logger log = LoggerFactory.getLogger(HadoopService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AnalysisJobRepository analysisJobRepository;
    private final FraudResultRepository fraudResultRepository;
    private final CallRecordRepository callRecordRepository;
    private final DatasetRepository datasetRepository;

    @Value("${telecom.hadoop.namenode.url:hdfs://localhost:9000}")
    private String nameNodeUrl;

    @Value("${telecom.hadoop.hdfs.input-path:/telecom/cdr/input}")
    private String hdfsInputPath;

    @Value("${telecom.hadoop.hdfs.output-path:/telecom/cdr/output}")
    private String hdfsOutputPath;

    @Value("${telecom.hadoop.local-workdir:./hadoop-work}")
    private String localWorkDir;

    public HadoopService(AnalysisJobRepository analysisJobRepository,
                         FraudResultRepository fraudResultRepository,
                         CallRecordRepository callRecordRepository,
                         DatasetRepository datasetRepository) {
        this.analysisJobRepository = analysisJobRepository;
        this.fraudResultRepository = fraudResultRepository;
        this.callRecordRepository = callRecordRepository;
        this.datasetRepository = datasetRepository;
    }

    @Transactional
    public Map<String, Object> loadSampleCDR(boolean force) {
        Map<String, Object> result = new HashMap<>();

        long existingCount = callRecordRepository.count();
        if (existingCount >= 1000 && !force) {
            result.put("success", false);
            result.put("alreadyLoaded", true);
            result.put("existingRecords", existingCount);
            result.put("message", "Sample CDR dataset is already imported (" + existingCount + " records in database). Do you want to reload and replace them?");
            return result;
        }

        if (force && existingCount > 0) {
            callRecordRepository.deleteAllInBatch();
        }

        File csvFile = new File("datasets/sample-cdr.csv");
        InputStream is = null;
        try {
            if (csvFile.exists()) {
                is = new FileInputStream(csvFile);
            } else {
                is = getClass().getResourceAsStream("/sample-cdr.csv");
            }

            if (is == null) {
                result.put("success", false);
                result.put("error", "Sample CDR dataset file (sample-cdr.csv) not found in datasets/ or classpath");
                return result;
            }

            List<CallRecord> validBatch = new ArrayList<>();
            long loadedRecords = 0;
            long rejectedRecords = 0;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                boolean firstLine = true;

                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("\uFEFF")) line = line.substring(1).trim();
                    if (line.isEmpty()) continue;

                    if (firstLine && (line.toLowerCase().startsWith("record_id") || line.toLowerCase().startsWith("caller_number"))) {
                        firstLine = false;
                        continue;
                    }
                    firstLine = false;

                    String[] parts = line.split(",", -1);
                    if (parts.length < 5) {
                        rejectedRecords++;
                        continue;
                    }

                    int idx = 0;
                    String recordId;
                    if (parts.length >= 10 && parts[0].toUpperCase().startsWith("CDR")) {
                        recordId = parts[0].trim();
                        idx = 1;
                    } else {
                        recordId = "CDR-" + (10000 + loadedRecords);
                    }

                    String rawCaller = parts[idx].trim();
                    String rawReceiver = parts[idx + 1].trim();
                    String caller = normalizeIndian(rawCaller);
                    String receiver = normalizeIndian(rawReceiver);

                    // Validate Indian mobile numbers: exactly 10 digits starting with 6, 7, 8, or 9
                    if (!caller.matches("^[6-9]\\d{9}$")) {
                        rejectedRecords++;
                        continue;
                    }

                    int duration = 0;
                    try {
                        duration = Integer.parseInt(parts[idx + 2].trim());
                    } catch (Exception e) {
                        duration = 0;
                    }

                    String callType = parts[idx + 3].trim().toUpperCase();
                    if (callType.isEmpty()) callType = "OUTGOING";

                    LocalDateTime timestamp;
                    try {
                        timestamp = LocalDateTime.parse(parts[idx + 4].trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    } catch (Exception e) {
                        timestamp = LocalDateTime.now();
                    }

                    CallRecord record = new CallRecord();
                    record.setRecordId(recordId);
                    record.setCallerNumber(caller);
                    record.setReceiverNumber(receiver);
                    record.setCallDuration(duration);
                    record.setCallType(callType);
                    record.setCallTimestamp(timestamp);
                    record.setDatasetId(1L);

                    if (parts.length >= idx + 6) record.setSourceRegion(parts[idx + 5].trim());
                    if (parts.length >= idx + 7) record.setDestinationRegion(parts[idx + 6].trim());
                    if (parts.length >= idx + 8) record.setIpAddress(parts[idx + 7].trim());
                    if (parts.length >= idx + 9) record.setNetworkType(parts[idx + 8].trim());

                    validBatch.add(record);
                    loadedRecords++;

                    if (validBatch.size() >= 500) {
                        callRecordRepository.saveAll(validBatch);
                        validBatch.clear();
                    }
                }

                if (!validBatch.isEmpty()) {
                    callRecordRepository.saveAll(validBatch);
                    validBatch.clear();
                }
            }

            // Update/Save Dataset Entity
            DatasetEntity dataset = datasetRepository.findByName("Indian-Telecom-CDR-Primary")
                    .orElse(new DatasetEntity());
            dataset.setName("Indian-Telecom-CDR-Primary");
            dataset.setFileName("sample-cdr.csv");
            dataset.setRecordCount((int) loadedRecords);
            dataset.setFileSizeKb(csvFile.exists() ? Math.round((csvFile.length() / 1024.0) * 10.0) / 10.0 : 84.5);
            dataset.setHdfsPath("/telecom/cdr/input/sample-cdr.csv");
            dataset.setUploadStatus("READY");
            dataset.setUploadedBy("admin");
            datasetRepository.save(dataset);

            // Attempt to stage/store to HDFS or local Hadoop staging
            boolean hdfsStaged = false;
            try {
                File workDir = new File(localWorkDir, "input");
                if (!workDir.exists()) workDir.mkdirs();
                File dest = new File(workDir, "sample-cdr.csv");
                if (csvFile.exists()) {
                    java.nio.file.Files.copy(csvFile.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    hdfsStaged = true;
                }
            } catch (Exception e) {
                log.warn("Could not copy sample CDR to local Hadoop staging: {}", e.getMessage());
            }

            result.put("success", true);
            result.put("alreadyLoaded", false);
            result.put("loadedRecords", loadedRecords);
            result.put("rejectedRecords", rejectedRecords);
            result.put("totalRecords", loadedRecords + rejectedRecords);
            result.put("datasetName", "Indian-Telecom-CDR-Primary");
            result.put("hdfsPath", "/telecom/cdr/input/sample-cdr.csv");
            result.put("hdfsStaged", hdfsStaged);
            result.put("message", "Successfully imported " + loadedRecords + " Indian CDR records into MySQL database (" + rejectedRecords + " rejected).");

        } catch (Exception e) {
            log.error("Failed to load sample CDR: ", e);
            result.put("success", false);
            result.put("error", "Failed to load sample CDR: " + e.getMessage());
        }

        return result;
    }

    public HadoopStatusDTO getHadoopStatus() {
        HadoopStatusDTO dto = new HadoopStatusDTO();
        dto.setNameNodeUrl(nameNodeUrl);
        dto.setHdfsInputPath(hdfsInputPath);
        dto.setHdfsOutputPath(hdfsOutputPath);

        Map<String, Object> details = new HashMap<>();
        boolean hdfsOnline = false;

        try {
            URI uri = URI.create(nameNodeUrl);
            String host = uri.getHost() != null ? uri.getHost() : "localhost";
            int port = uri.getPort() != -1 ? uri.getPort() : 9000;

            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 1000);
                hdfsOnline = true;
                details.put("socketConnection", "SUCCESS (Port " + port + " reachable)");
            } catch (Exception e) {
                hdfsOnline = false;
                details.put("socketConnection", "OFFLINE (Port " + port + " not reachable: " + e.getMessage() + ")");
            }
        } catch (Exception e) {
            hdfsOnline = false;
            details.put("socketConnection", "INVALID_URI: " + e.getMessage());
        }

        dto.setHadoopConnected(hdfsOnline);
        dto.setHdfsConnected(hdfsOnline);
        dto.setClusterStatus(hdfsOnline ? "ONLINE_ACTIVE" : "STANDALONE_LOCAL_READY");

        long completedJobs = analysisJobRepository.countByStatus("COMPLETED");
        long runningJobs = analysisJobRepository.countByStatus("RUNNING");
        dto.setActiveJobStatus(runningJobs > 0 ? "RUNNING (" + runningJobs + " active)" : "IDLE (Ready for job submission)");

        List<AnalysisJob> recent = analysisJobRepository.findByOrderByStartTimeDesc();
        dto.setLastExecutionTime(!recent.isEmpty() ? recent.get(0).getStartTime().format(DATE_FORMAT) : "N/A");
        dto.setTotalHdfsFiles(completedJobs);

        details.put("localExecutionSupported", true);
        details.put("hadoopVersion", "Apache Hadoop 3.3.6");
        details.put("driverClass", "com.telecom.fraud.mapreduce.FraudDetectionDriver");
        details.put("mapperClass", "com.telecom.fraud.mapreduce.CDRFraudMapper");
        details.put("reducerClass", "com.telecom.fraud.mapreduce.CDRFraudReducer");

        dto.setConnectionDetails(details);
        return dto;
    }

    public synchronized JobStatusDTO startMapReduceJob(JobTriggerRequest request) {
        String timestampId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String jobId = "JOB_HADOOP_" + timestampId;
        String datasetName = (request.getDatasetName() != null && !request.getDatasetName().trim().isEmpty())
                ? request.getDatasetName().trim() : "Indian-Telecom-CDR-Primary";

        long startTimeMs = System.currentTimeMillis();
        AnalysisJob job = new AnalysisJob();
        job.setJobId(jobId);
        job.setDatasetName(datasetName);
        job.setStartTime(LocalDateTime.now());
        job.setStatus("RUNNING");
        job.setInputRecords(1000);
        job = analysisJobRepository.save(job);

        JobStatusDTO statusDTO = new JobStatusDTO();
        statusDTO.setJobId(jobId);
        statusDTO.setDatasetName(datasetName);
        statusDTO.setStatus("RUNNING");
        statusDTO.setStartTime(job.getStartTime().format(DATE_FORMAT));

        Map<String, Long> countersMap = new HashMap<>();

        try {
            File inputFile = new File(request.getInputPath() != null ? request.getInputPath() : "datasets/sample-cdr.csv");
            if (!inputFile.exists()) {
                inputFile = new File("datasets/sample-cdr.csv");
            }

            File workDir = new File(localWorkDir, jobId);
            if (!workDir.exists()) {
                workDir.mkdirs();
            }
            File outputDir = new File(workDir, "output");

            log.info("Starting Hadoop MapReduce Execution for Job ID: {}", jobId);

            // Execute MapReduce pipeline
            executeMapReducePipeline(inputFile, outputDir, request, countersMap, jobId);

            long endTimeMs = System.currentTimeMillis();
            long durationSec = Math.max(1, (endTimeMs - startTimeMs) / 1000);

            long validRecords = countersMap.getOrDefault("ValidRecordsProcessed", 1000L);
            long highRisk = countersMap.getOrDefault("HighRiskDetected", 0L);
            long medRisk = countersMap.getOrDefault("MediumRiskDetected", 0L);
            long totalFraud = highRisk + medRisk;

            job.setStatus("COMPLETED");
            job.setEndTime(LocalDateTime.now());
            job.setInputRecords((int) validRecords);
            job.setProcessedRecords((int) validRecords);
            job.setFraudRecords((int) totalFraud);
            job.setDurationSeconds(durationSec);
            analysisJobRepository.save(job);

            statusDTO.setStatus("COMPLETED");
            statusDTO.setEndTime(job.getEndTime().format(DATE_FORMAT));
            statusDTO.setInputRecords((int) validRecords);
            statusDTO.setProcessedRecords((int) validRecords);
            statusDTO.setFraudRecords((int) totalFraud);
            statusDTO.setDurationSeconds(durationSec);
            statusDTO.setCounters(countersMap);

        } catch (Exception e) {
            log.error("Hadoop MapReduce Job failed: ", e);
            job.setStatus("FAILED");
            job.setEndTime(LocalDateTime.now());
            job.setErrorDetails(e.getMessage());
            analysisJobRepository.save(job);

            statusDTO.setStatus("FAILED");
            statusDTO.setErrorDetails(e.getMessage());
        }

        return statusDTO;
    }

    private static class LocalCdrRecord {
        String receiverNumber;
        int duration;
        String callTime;
        String callType;
        boolean nightCall;

        LocalCdrRecord(String receiver, int dur, String time, String type, boolean night) {
            this.receiverNumber = receiver;
            this.duration = dur;
            this.callTime = time;
            this.callType = type;
            this.nightCall = night;
        }
    }

    private void executeMapReducePipeline(File inputFile, File outputDir, JobTriggerRequest request,
                                         Map<String, Long> counters, String jobId) throws Exception {
        outputDir.mkdirs();
        File outputFile = new File(outputDir, "part-r-00000");

        // MAPPER PHASE: reads CSV, validates Indian numbers, emits (caller, list of records)
        Map<String, List<LocalCdrRecord>> mapOutput = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), StandardCharsets.UTF_8))) {
            String line;
            boolean firstLine = true;
            long validCount = 0;
            long malformedCount = 0;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("\uFEFF")) line = line.substring(1).trim();
                if (line.isEmpty()) continue;
                if (firstLine && (line.toLowerCase().startsWith("record_id") || line.toLowerCase().startsWith("caller_number"))) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;

                String[] parts = line.split(",", -1);
                if (parts.length < 5) {
                    malformedCount++;
                    continue;
                }

                int idx = 0;
                if (parts.length >= 10 && parts[0].toUpperCase().startsWith("CDR")) {
                    idx = 1;
                }

                String caller = normalizeIndian(parts[idx].trim());
                String receiver = normalizeIndian(parts[idx + 1].trim());

                if (!caller.matches("^[6-9]\\d{9}$")) {
                    malformedCount++;
                    continue;
                }

                int duration = 0;
                try {
                    duration = Integer.parseInt(parts[idx + 2].trim());
                } catch (Exception ignored) {}

                String callType = parts[idx + 3].trim().toUpperCase();
                String timestamp = parts[idx + 4].trim();

                boolean isNight = false;
                if (timestamp.contains(" 00:") || timestamp.contains(" 01:") ||
                    timestamp.contains(" 02:") || timestamp.contains(" 03:") ||
                    timestamp.contains(" 04:")) {
                    isNight = true;
                }

                LocalCdrRecord record = new LocalCdrRecord(receiver, duration, timestamp, callType, isNight);
                mapOutput.computeIfAbsent(caller, k -> new ArrayList<>()).add(record);
                validCount++;
            }

            counters.put("ValidRecordsProcessed", validCount);
            counters.put("MalformedRecords", malformedCount);
        }

        // REDUCER PHASE: aggregates metrics and evaluates multi-factor fraud risk scores
        int freqThreshold = request.getFrequencyThreshold();
        int distinctThreshold = request.getDistinctReceiversThreshold();
        int wangiriDuration = request.getWangiriDurationThreshold();
        int excessiveDuration = request.getExcessiveDurationThreshold();
        double nightRatioThreshold = request.getNightRatioThreshold();

        long highRiskCount = 0;
        long medRiskCount = 0;
        long lowRiskCount = 0;
        long normalCount = 0;

        List<FraudResult> fraudEntities = new ArrayList<>();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {
            for (Map.Entry<String, List<LocalCdrRecord>> entry : mapOutput.entrySet()) {
                String caller = entry.getKey();
                List<LocalCdrRecord> records = entry.getValue();

                int totalCalls = records.size();
                long totalDuration = 0;
                int intlCalls = 0;
                int nightCalls = 0;
                Set<String> distinctReceivers = new HashSet<>();

                for (LocalCdrRecord r : records) {
                    totalDuration += r.duration;
                    distinctReceivers.add(r.receiverNumber);
                    if (r.callType.contains("INT") || r.callType.contains("ISD")) intlCalls++;
                    if (r.nightCall) nightCalls++;
                }

                int distinctCount = distinctReceivers.size();
                double avgDuration = totalCalls > 0 ? (double) totalDuration / totalCalls : 0.0;
                double nightRatio = totalCalls > 0 ? (double) nightCalls / totalCalls : 0.0;

                int score = 0;
                List<String> reasons = new ArrayList<>();

                if (totalCalls >= (freqThreshold * 2)) {
                    score += 30;
                    reasons.add("Abnormally high calling volume (" + totalCalls + " calls)");
                } else if (totalCalls >= freqThreshold) {
                    score += 20;
                    reasons.add("Elevated call frequency (" + totalCalls + " calls)");
                }

                if (distinctCount >= (distinctThreshold * 2)) {
                    score += 30;
                    reasons.add("Excessive distinct receivers (" + distinctCount + " targets)");
                } else if (distinctCount >= distinctThreshold) {
                    score += 20;
                    reasons.add("High distinct target count (" + distinctCount + " targets)");
                }

                if (totalCalls >= 4 && avgDuration <= wangiriDuration && distinctCount >= 3) {
                    score += 25;
                    reasons.add("Suspected Wangiri / Ping-Call pattern (avg " + String.format("%.1f", avgDuration) + "s)");
                }

                if (totalDuration >= excessiveDuration) {
                    score += 15;
                    reasons.add("Excessive total duration (" + (totalDuration / 60) + " minutes)");
                }

                if (totalCalls >= 5 && nightRatio >= nightRatioThreshold) {
                    score += 15;
                    reasons.add("Unusual night call ratio (" + String.format("%.1f%%", nightRatio * 100) + ")");
                }

                if (intlCalls >= 3) {
                    score += 15;
                    reasons.add("Frequent international dialing (" + intlCalls + " ISD calls)");
                }

                if (score > 100) score = 100;

                String riskLevel;
                if (score >= 70) {
                    riskLevel = "HIGH RISK";
                    highRiskCount++;
                } else if (score >= 40) {
                    riskLevel = "MEDIUM RISK";
                    medRiskCount++;
                } else if (score >= 15) {
                    riskLevel = "LOW RISK";
                    lowRiskCount++;
                } else {
                    riskLevel = "NORMAL";
                    reasons.add("Normal Subscriber Activity");
                    normalCount++;
                }

                String reasonStr = reasons.isEmpty() ? "Normal Activity" : String.join(" | ", reasons);

                // Write reducer output line
                writer.println(String.format("%s\t%d\t%d\t%d\t%.2f\t%d\t%d\t%d\t%s\t%s",
                        caller, totalCalls, distinctCount, totalDuration, avgDuration, intlCalls, nightCalls, score, riskLevel, reasonStr));

                // Save or update FraudResult in MySQL
                FraudResult fr = fraudResultRepository.findByMobileNumber(caller).orElse(new FraudResult());
                fr.setMobileNumber(caller);
                fr.setTotalCalls(totalCalls);
                fr.setDistinctReceivers(distinctCount);
                fr.setTotalDuration(totalDuration);
                fr.setAvgDuration(avgDuration);
                fr.setInternationalCalls(intlCalls);
                fr.setNightCalls(nightCalls);
                fr.setRiskScore(score);
                fr.setRiskLevel(riskLevel);
                fr.setRiskReasons(reasonStr);
                fr.setLastJobId(jobId);
                fraudEntities.add(fr);
            }
        }

        // Synchronize results to database
        fraudResultRepository.saveAll(fraudEntities);

        counters.put("HighRiskDetected", highRiskCount);
        counters.put("MediumRiskDetected", medRiskCount);
        counters.put("LowRiskDetected", lowRiskCount);
        counters.put("NormalSubscribers", normalCount);
        counters.put("TotalUniqueCallers", (long) mapOutput.size());
    }

    private String normalizeIndian(String raw) {
        if (raw == null) return "";
        String cleaned = raw.replaceAll("[^0-9]", "");
        if (cleaned.startsWith("91") && cleaned.length() == 12) {
            cleaned = cleaned.substring(2);
        } else if (cleaned.startsWith("0") && cleaned.length() == 11) {
            cleaned = cleaned.substring(1);
        }
        return cleaned;
    }
}
