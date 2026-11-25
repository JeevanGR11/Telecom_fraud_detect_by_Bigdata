package com.telecom.fraud.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Reducer for Telecom Fraud Detection: aggregates subscriber calling patterns,
 * computes multi-factor fraud risk score (0-100), and classifies potential fraud.
 */
public class CDRFraudReducer extends Reducer<Text, CDRRecordWritable, Text, Text> {

    private final Text resultValue = new Text();

    private int thresholdFrequency = 8;
    private int thresholdDistinctReceivers = 6;
    private int thresholdWangiriAvgDuration = 5;
    private int thresholdExcessiveDuration = 3600;
    private double thresholdNightRatio = 0.35;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration conf = context.getConfiguration();
        thresholdFrequency = conf.getInt("fraud.threshold.frequency", 8);
        thresholdDistinctReceivers = conf.getInt("fraud.threshold.distinct.receivers", 6);
        thresholdWangiriAvgDuration = conf.getInt("fraud.threshold.wangiri.avg.duration", 5);
        thresholdExcessiveDuration = conf.getInt("fraud.threshold.excessive.duration", 3600);
        thresholdNightRatio = conf.getDouble("fraud.threshold.night.ratio", 0.35);
    }

    @Override
    protected void reduce(Text key, Iterable<CDRRecordWritable> values, Context context)
            throws IOException, InterruptedException {

        int totalCalls = 0;
        long totalDuration = 0;
        int internationalCalls = 0;
        int nightCalls = 0;
        Set<String> distinctReceivers = new HashSet<>();

        for (CDRRecordWritable val : values) {
            totalCalls++;
            totalDuration += val.getDuration();
            distinctReceivers.add(val.getReceiverNumber());

            String type = val.getCallType().toUpperCase();
            if (type.contains("INT") || type.contains("ISD")) {
                internationalCalls++;
            }

            if (val.isNightCall()) {
                nightCalls++;
            }
        }

        int distinctReceiverCount = distinctReceivers.size();
        double avgDuration = totalCalls > 0 ? (double) totalDuration / totalCalls : 0.0;
        double nightRatio = totalCalls > 0 ? (double) nightCalls / totalCalls : 0.0;

        // Weighted Risk Scoring (0 to 100)
        int riskScore = 0;
        List<String> reasons = new ArrayList<>();

        // 1. High calling frequency
        if (totalCalls >= (thresholdFrequency * 2)) {
            riskScore += 30;
            reasons.add("Abnormally high calling volume (" + totalCalls + " calls)");
        } else if (totalCalls >= thresholdFrequency) {
            riskScore += 20;
            reasons.add("Elevated call frequency (" + totalCalls + " calls)");
        }

        // 2. High distinct receivers (robocall / blast pattern)
        if (distinctReceiverCount >= (thresholdDistinctReceivers * 2)) {
            riskScore += 30;
            reasons.add("Excessive distinct receivers (" + distinctReceiverCount + " targets)");
        } else if (distinctReceiverCount >= thresholdDistinctReceivers) {
            riskScore += 20;
            reasons.add("High distinct target count (" + distinctReceiverCount + " targets)");
        }

        // 3. Wangiri / Ping-Call detection (very short avg duration to many distinct receivers)
        if (totalCalls >= 4 && avgDuration <= thresholdWangiriAvgDuration && distinctReceiverCount >= 3) {
            riskScore += 25;
            reasons.add("Suspected Wangiri / Ping-Call pattern (avg " + String.format("%.1f", avgDuration) + "s)");
        }

        // 4. Excessive call duration (toll leakage / conference fraud)
        if (totalDuration >= thresholdExcessiveDuration) {
            riskScore += 15;
            reasons.add("Excessive total duration (" + (totalDuration / 60) + " minutes)");
        }

        // 5. Abnormal night calling activity
        if (totalCalls >= 5 && nightRatio >= thresholdNightRatio) {
            riskScore += 15;
            reasons.add("Unusual night call ratio (" + String.format("%.1f%%", nightRatio * 100) + ")");
        }

        // 6. International call spam
        if (internationalCalls >= 3) {
            riskScore += 15;
            reasons.add("Frequent international dialing (" + internationalCalls + " ISD calls)");
        }

        if (riskScore > 100) {
            riskScore = 100;
        }

        // Risk classification
        String riskLevel;
        if (riskScore >= 70) {
            riskLevel = "HIGH RISK";
            context.getCounter("TelecomFraud", "HighRiskDetected").increment(1);
        } else if (riskScore >= 40) {
            riskLevel = "MEDIUM RISK";
            context.getCounter("TelecomFraud", "MediumRiskDetected").increment(1);
        } else if (riskScore >= 15) {
            riskLevel = "LOW RISK";
            context.getCounter("TelecomFraud", "LowRiskDetected").increment(1);
        } else {
            riskLevel = "NORMAL";
            reasons.add("Normal Subscriber Activity");
            context.getCounter("TelecomFraud", "NormalSubscribers").increment(1);
        }

        String reasonJoined = reasons.isEmpty() ? "Normal Activity" : String.join(" | ", reasons);

        // Format: totalCalls \t distinctReceivers \t totalDuration \t avgDuration \t internationalCalls \t nightCalls \t riskScore \t riskLevel \t reasons
        String formattedOutput = String.format("%d\t%d\t%d\t%.2f\t%d\t%d\t%d\t%s\t%s",
                totalCalls,
                distinctReceiverCount,
                totalDuration,
                avgDuration,
                internationalCalls,
                nightCalls,
                riskScore,
                riskLevel,
                reasonJoined);

        resultValue.set(formattedOutput);
        context.write(key, resultValue);
        context.getCounter("TelecomFraud", "TotalUniqueCallers").increment(1);
    }
}
