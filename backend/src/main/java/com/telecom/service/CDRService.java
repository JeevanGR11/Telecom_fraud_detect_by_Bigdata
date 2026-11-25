package com.telecom.service;

import com.telecom.entity.CallRecord;
import com.telecom.repository.CallRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CDRService {

    private final CallRecordRepository callRecordRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CDRService(CallRecordRepository callRecordRepository) {
        this.callRecordRepository = callRecordRepository;
    }

    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void initSeedData() {
        if (callRecordRepository.count() == 0) {
            try {
                InputStream is = getClass().getResourceAsStream("/sample-cdr.csv");
                if (is != null) {
                    int imported = importRecordsFromCsv(is, 1L);
                    System.out.println("Seeded " + imported + " initial CDR records from /sample-cdr.csv");
                } else {
                    java.io.File file = new java.io.File("datasets/sample-cdr.csv");
                    if (file.exists()) {
                        try (InputStream fis = new java.io.FileInputStream(file)) {
                            int imported = importRecordsFromCsv(fis, 1L);
                            System.out.println("Seeded " + imported + " initial CDR records from " + file.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: Could not seed initial CDR records: " + e.getMessage());
            }
        }
    }

    public Page<CallRecord> getRecords(String caller, String receiver, String callType,
                                       String startDate, String endDate,
                                       int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction != null ? direction : "DESC"),
                sortBy != null ? sortBy : "callTimestamp");
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime start = null;
        LocalDateTime end = null;
        try {
            if (startDate != null && !startDate.trim().isEmpty()) {
                start = LocalDateTime.parse(startDate.trim() + " 00:00:00", FORMATTER);
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                end = LocalDateTime.parse(endDate.trim() + " 23:59:59", FORMATTER);
            }
        } catch (Exception ignored) {}

        String callerFilter = (caller != null && !caller.trim().isEmpty()) ? normalizeNumber(caller.trim()) : null;
        String receiverFilter = (receiver != null && !receiver.trim().isEmpty()) ? normalizeNumber(receiver.trim()) : null;
        String typeFilter = (callType != null && !callType.trim().isEmpty() && !"ALL".equalsIgnoreCase(callType)) ? callType.trim().toUpperCase() : null;

        return callRecordRepository.searchRecords(callerFilter, receiverFilter, typeFilter, start, end, pageable);
    }

    public CallRecord getRecordById(Long id) {
        return callRecordRepository.findById(id).orElse(null);
    }

    @Transactional
    public int importRecordsFromCsv(InputStream inputStream, Long datasetId) {
        List<CallRecord> batch = new ArrayList<>();
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
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
                if (parts.length < 5) continue;

                CallRecord record = new CallRecord();
                int idx = 0;
                if (parts.length >= 10 && parts[0].toUpperCase().startsWith("CDR")) {
                    record.setRecordId(parts[0].trim());
                    idx = 1;
                } else {
                    record.setRecordId("CDR-" + (10000 + count));
                }

                String caller = normalizeNumber(parts[idx].trim());
                String receiver = normalizeNumber(parts[idx + 1].trim());
                int duration = 0;
                try {
                    duration = Integer.parseInt(parts[idx + 2].trim());
                } catch (Exception ignored) {}

                String callType = parts[idx + 3].trim().toUpperCase();
                String timestampStr = parts[idx + 4].trim();
                LocalDateTime dt;
                try {
                    dt = LocalDateTime.parse(timestampStr, FORMATTER);
                } catch (Exception e) {
                    dt = LocalDateTime.now();
                }

                record.setCallerNumber(caller);
                record.setReceiverNumber(receiver);
                record.setCallDuration(duration);
                record.setCallType(callType);
                record.setCallTimestamp(dt);
                record.setDatasetId(datasetId);

                if (parts.length >= idx + 6) record.setSourceRegion(parts[idx + 5].trim());
                if (parts.length >= idx + 7) record.setDestinationRegion(parts[idx + 6].trim());
                if (parts.length >= idx + 8) record.setIpAddress(parts[idx + 7].trim());
                if (parts.length >= idx + 9) record.setNetworkType(parts[idx + 8].trim());

                batch.add(record);
                count++;

                if (batch.size() >= 500) {
                    callRecordRepository.saveAll(batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                callRecordRepository.saveAll(batch);
                batch.clear();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading CSV stream", e);
        }

        return count;
    }

    public String exportAllToCsv() {
        StringBuilder sb = new StringBuilder();
        sb.append("record_id,caller_number,receiver_number,call_duration,call_type,call_timestamp,source_region,destination_region,ip_address,network_type\n");

        List<CallRecord> records = callRecordRepository.findAll();
        for (CallRecord r : records) {
            sb.append(String.format("%s,%s,%s,%d,%s,%s,%s,%s,%s,%s\n",
                    r.getRecordId(),
                    r.getCallerNumber(),
                    r.getReceiverNumber(),
                    r.getCallDuration(),
                    r.getCallType(),
                    r.getCallTimestamp() != null ? r.getCallTimestamp().format(FORMATTER) : "",
                    r.getSourceRegion() != null ? r.getSourceRegion().replace(",", " -") : "",
                    r.getDestinationRegion() != null ? r.getDestinationRegion().replace(",", " -") : "",
                    r.getIpAddress() != null ? r.getIpAddress() : "",
                    r.getNetworkType() != null ? r.getNetworkType() : ""));
        }
        return sb.toString();
    }

    public static String normalizeNumber(String raw) {
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
