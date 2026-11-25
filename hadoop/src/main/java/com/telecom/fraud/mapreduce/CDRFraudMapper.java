package com.telecom.fraud.mapreduce;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Mapper for Telecom Fraud Detection: parses Indian CDR records and emits (caller, CDRRecordWritable).
 */
public class CDRFraudMapper extends Mapper<LongWritable, Text, Text, CDRRecordWritable> {

    private final Text callerKey = new Text();
    private final CDRRecordWritable cdrValue = new CDRRecordWritable();

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Pattern INDIAN_PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$");

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.startsWith("\uFEFF")) { // Remove UTF-8 BOM if present
            line = line.substring(1).trim();
        }

        if (line.isEmpty() || line.toLowerCase().startsWith("caller_number") || line.toLowerCase().startsWith("record_id")) {
            return; // Skip empty lines and headers
        }

        String[] parts = line.split(",", -1);
        if (parts.length < 5) {
            context.getCounter("TelecomFraud", "MalformedRecords").increment(1);
            return;
        }

        // Support both schemas:
        // Schema A (5 columns): caller_number, receiver_number, duration, call_type, call_timestamp
        // Schema B (10 columns): record_id, caller_number, receiver_number, duration, call_type, call_timestamp, source_region, destination_region, ip_address, network_type
        int callerIdx = 0;
        int receiverIdx = 1;
        int durationIdx = 2;
        int typeIdx = 3;
        int timeIdx = 4;

        if (parts.length >= 10 && parts[0].toUpperCase().startsWith("CDR")) {
            callerIdx = 1;
            receiverIdx = 2;
            durationIdx = 3;
            typeIdx = 4;
            timeIdx = 5;
        }

        String caller = normalizeIndianNumber(parts[callerIdx].trim());
        String receiver = normalizeIndianNumber(parts[receiverIdx].trim());
        String durationStr = parts[durationIdx].trim();
        String callType = parts[typeIdx].trim().toUpperCase();
        String callTime = parts[timeIdx].trim();

        // Validate caller number as valid Indian 10-digit mobile
        if (!INDIAN_PHONE_PATTERN.matcher(caller).matches()) {
            context.getCounter("TelecomFraud", "InvalidCallerNumbers").increment(1);
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration < 0) duration = 0;
        } catch (NumberFormatException e) {
            context.getCounter("TelecomFraud", "MalformedDuration").increment(1);
            return;
        }

        // Night call detection (between 00:00:00 and 04:59:59)
        boolean isNight = false;
        try {
            LocalDateTime dt = LocalDateTime.parse(callTime, FORMATTER);
            int hour = dt.getHour();
            if (hour >= 0 && hour <= 4) {
                isNight = true;
            }
        } catch (DateTimeParseException e) {
            if (callTime.contains(" 00:") || callTime.contains(" 01:") ||
                callTime.contains(" 02:") || callTime.contains(" 03:") ||
                callTime.contains(" 04:")) {
                isNight = true;
            }
        }

        cdrValue.setReceiverNumber(receiver);
        cdrValue.setDuration(duration);
        cdrValue.setCallTime(callTime);
        cdrValue.setCallType(callType);
        cdrValue.setNightCall(isNight);

        callerKey.set(caller);
        context.write(callerKey, cdrValue);
        context.getCounter("TelecomFraud", "ValidRecordsProcessed").increment(1);
    }

    private String normalizeIndianNumber(String raw) {
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
