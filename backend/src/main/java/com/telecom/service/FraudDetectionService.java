package com.telecom.service;

import com.telecom.dto.SearchFraudDTO;
import com.telecom.dto.SearchFraudDTO.CallItemDTO;
import com.telecom.entity.CallRecord;
import com.telecom.entity.FraudResult;
import com.telecom.repository.CallRecordRepository;
import com.telecom.repository.FraudResultRepository;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class FraudDetectionService {

    private final CallRecordRepository callRecordRepository;
    private final FraudResultRepository fraudResultRepository;

    private static final Pattern INDIAN_MOBILE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public FraudDetectionService(CallRecordRepository callRecordRepository,
                                 FraudResultRepository fraudResultRepository) {
        this.callRecordRepository = callRecordRepository;
        this.fraudResultRepository = fraudResultRepository;
    }

    public SearchFraudDTO searchNumber(String rawNumber) {
        SearchFraudDTO response = new SearchFraudDTO();

        if (rawNumber == null || rawNumber.trim().isEmpty()) {
            response.setFound(false);
            response.setMessage("Please enter an Indian mobile number.");
            return response;
        }

        String normalized = CDRService.normalizeNumber(rawNumber.trim());

        if (!INDIAN_MOBILE_PATTERN.matcher(normalized).matches()) {
            response.setFound(false);
            response.setMessage("Invalid Indian mobile number format. Number must be 10 digits starting with 6, 7, 8, or 9 (optional +91).");
            return response;
        }

        response.setMobileNumber(normalized);

        // Fetch calls where this number is caller or receiver
        List<CallRecord> outboundCalls = callRecordRepository.findByCallerNumberOrderByCallTimestampDesc(normalized);
        List<CallRecord> inboundCalls = callRecordRepository.findByReceiverNumberOrderByCallTimestampDesc(normalized);

        if (outboundCalls.isEmpty() && inboundCalls.isEmpty()) {
            response.setFound(false);
            response.setMessage("No records found for this mobile number.");
            return response;
        }

        response.setFound(true);
        response.setMessage("Records found and analyzed successfully.");

        int totalCalls = outboundCalls.size() + inboundCalls.size();
        int outgoingCount = outboundCalls.size();
        int incomingCount = inboundCalls.size();
        int intlCount = 0;
        int missedCount = 0;
        long totalDuration = 0;
        int nightCalls = 0;
        Set<String> distinctContacts = new HashSet<>();

        Map<String, Integer> hourlyMap = new TreeMap<>();
        for (int h = 0; h < 24; h++) {
            hourlyMap.put(String.format("%02d:00", h), 0);
        }

        Map<String, Integer> typeMap = new HashMap<>();
        List<CallItemDTO> callHistory = new ArrayList<>();

        for (CallRecord c : outboundCalls) {
            totalDuration += c.getCallDuration();
            distinctContacts.add(c.getReceiverNumber());
            String type = c.getCallType().toUpperCase();
            typeMap.put(type, typeMap.getOrDefault(type, 0) + 1);

            if (type.contains("INT") || type.contains("ISD")) intlCount++;
            if (type.contains("MISSED")) missedCount++;

            if (c.getCallTimestamp() != null) {
                int hour = c.getCallTimestamp().getHour();
                String hKey = String.format("%02d:00", hour);
                hourlyMap.put(hKey, hourlyMap.getOrDefault(hKey, 0) + 1);
                if (hour >= 0 && hour <= 4) nightCalls++;
            }

            CallItemDTO item = new CallItemDTO();
            item.setRecordId(c.getRecordId());
            item.setReceiverNumber(c.getReceiverNumber());
            item.setCallType(c.getCallType());
            item.setDuration(c.getCallDuration());
            item.setTimestamp(c.getCallTimestamp() != null ? c.getCallTimestamp().format(FORMATTER) : "");
            item.setLocation(c.getSourceRegion() != null ? c.getSourceRegion() : "India");
            item.setIpAddress(c.getIpAddress() != null ? c.getIpAddress() : "N/A");
            item.setSuspicious(c.getCallDuration() <= 5 && "OUTGOING".equalsIgnoreCase(c.getCallType()));
            callHistory.add(item);
        }

        for (CallRecord c : inboundCalls) {
            distinctContacts.add(c.getCallerNumber());
            String type = c.getCallType().toUpperCase();
            typeMap.put("INCOMING", typeMap.getOrDefault("INCOMING", 0) + 1);

            CallItemDTO item = new CallItemDTO();
            item.setRecordId(c.getRecordId());
            item.setReceiverNumber(c.getCallerNumber() + " (Caller)");
            item.setCallType("INCOMING");
            item.setDuration(c.getCallDuration());
            item.setTimestamp(c.getCallTimestamp() != null ? c.getCallTimestamp().format(FORMATTER) : "");
            item.setLocation(c.getDestinationRegion() != null ? c.getDestinationRegion() : "India");
            item.setIpAddress(c.getIpAddress() != null ? c.getIpAddress() : "N/A");
            item.setSuspicious(false);
            callHistory.add(item);
        }

        double avgDuration = totalCalls > 0 ? (double) totalDuration / totalCalls : 0.0;
        int distinctCount = distinctContacts.size();

        // Check if there is an aggregated FraudResult from MapReduce
        Optional<FraudResult> frOpt = fraudResultRepository.findByMobileNumber(normalized);

        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        if (frOpt.isPresent()) {
            FraudResult fr = frOpt.get();
            riskScore = fr.getRiskScore();
            if (fr.getRiskReasons() != null && !fr.getRiskReasons().isEmpty()) {
                reasons = Arrays.asList(fr.getRiskReasons().split(" \\| "));
            }
        } else {
            // Calculate risk dynamically using the standard transparent rules
            if (outgoingCount >= 16) {
                riskScore += 30;
                reasons.add("Abnormally high calling volume (" + outgoingCount + " outbound calls)");
            } else if (outgoingCount >= 8) {
                riskScore += 20;
                reasons.add("Elevated call frequency (" + outgoingCount + " calls)");
            }

            if (distinctCount >= 12) {
                riskScore += 30;
                reasons.add("Excessive distinct receivers (" + distinctCount + " targets)");
            } else if (distinctCount >= 6) {
                riskScore += 20;
                reasons.add("High distinct target count (" + distinctCount + " targets)");
            }

            if (outgoingCount >= 4 && avgDuration <= 5.0 && distinctCount >= 3) {
                riskScore += 25;
                reasons.add("Suspected Wangiri / Ping-Call pattern (avg " + String.format("%.1f", avgDuration) + "s)");
            }

            if (totalDuration >= 3600) {
                riskScore += 15;
                reasons.add("Excessive total duration (" + (totalDuration / 60) + " minutes)");
            }

            double nightRatio = outgoingCount > 0 ? (double) nightCalls / outgoingCount : 0.0;
            if (outgoingCount >= 5 && nightRatio >= 0.35) {
                riskScore += 15;
                reasons.add("Unusual night call ratio (" + String.format("%.1f%%", nightRatio * 100) + ")");
            }

            if (intlCount >= 3) {
                riskScore += 15;
                reasons.add("Frequent international dialing (" + intlCount + " ISD calls)");
            }

            if (riskScore > 100) riskScore = 100;
        }

        String classification;
        if (riskScore >= 70) {
            classification = "HIGH RISK";
        } else if (riskScore >= 40) {
            classification = "MEDIUM RISK";
        } else {
            classification = "LOW RISK";
            if (reasons.isEmpty()) reasons.add("Normal Subscriber Activity");
        }

        response.setTotalCalls(totalCalls);
        response.setIncomingCalls(incomingCount);
        response.setOutgoingCalls(outgoingCount);
        response.setInternationalCalls(intlCount);
        response.setMissedCalls(missedCount);
        response.setTotalDurationSeconds(totalDuration);
        response.setAvgDurationSeconds(Math.round(avgDuration * 100.0) / 100.0);
        response.setDistinctReceiversCount(distinctCount);
        response.setSuspiciousCallsCount(riskScore >= 40 ? outgoingCount : 0);
        response.setNightCallsCount(nightCalls);
        response.setRiskScore(riskScore);
        response.setRiskClassification(classification);
        response.setAlertReasons(reasons);
        response.setCallHistory(callHistory);
        response.setHourlyDistribution(hourlyMap);
        response.setCallTypeDistribution(typeMap);

        return response;
    }

    public List<FraudResult> getAllFraudResults() {
        return fraudResultRepository.findAll();
    }
}
