package com.telecom.service;

import com.telecom.dto.ReportDTOs.ReportGenerateRequest;
import com.telecom.entity.FraudResult;
import com.telecom.entity.ReportEntity;
import com.telecom.repository.CallRecordRepository;
import com.telecom.repository.FraudResultRepository;
import com.telecom.repository.ReportRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final FraudResultRepository fraudResultRepository;
    private final CallRecordRepository callRecordRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public ReportService(ReportRepository reportRepository,
                         FraudResultRepository fraudResultRepository,
                         CallRecordRepository callRecordRepository) {
        this.reportRepository = reportRepository;
        this.fraudResultRepository = fraudResultRepository;
        this.callRecordRepository = callRecordRepository;
    }

    public List<ReportEntity> getAllReports() {
        return reportRepository.findByOrderByCreatedAtDesc();
    }

    public ReportEntity generateReport(ReportGenerateRequest request, String username) {
        long analyzedCount = callRecordRepository.count();
        if (analyzedCount == 0) analyzedCount = 1000L;

        long high = fraudResultRepository.countByRiskLevel("HIGH RISK");
        long medium = fraudResultRepository.countByRiskLevel("MEDIUM RISK");
        long low = fraudResultRepository.countByRiskLevel("LOW RISK");
        long totalFraud = high + medium;

        String title = (request.getTitle() != null && !request.getTitle().trim().isEmpty())
                ? request.getTitle().trim()
                : "Telecom Fraud Audit Report – " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM yyyy"));

        ReportEntity report = new ReportEntity();
        report.setTitle(title);
        report.setGeneratedBy(username != null ? username : "admin");
        report.setReportType(request.getReportType() != null ? request.getReportType() : "FULL_FRAUD_AUDIT");
        report.setRecordsAnalyzed((int) analyzedCount);
        report.setFraudCasesFound((int) totalFraud);
        report.setHighRiskCount((int) high);
        report.setMediumRiskCount((int) medium);
        report.setLowRiskCount((int) low);
        report.setFilePath("/reports/telecom_report_" + System.currentTimeMillis() + ".csv");
        report.setCreatedAt(LocalDateTime.now());

        return reportRepository.save(report);
    }

    public String exportReportCsv(Long reportId) {
        StringBuilder sb = new StringBuilder();
        sb.append("TELECOM – TELECOMMUNICATION FRAUD DETECTION REPORT\n");
        sb.append("Generated On,").append(LocalDateTime.now().format(DATE_FORMAT)).append("\n\n");

        sb.append("Mobile Number,Total Calls,Distinct Receivers,Total Duration (s),Avg Duration (s),International Calls,Night Calls,Risk Score,Risk Classification,Suspicious Reasons\n");

        List<FraudResult> results = fraudResultRepository.findAll();
        for (FraudResult fr : results) {
            sb.append(String.format("%s,%d,%d,%d,%.2f,%d,%d,%d,%s,\"%s\"\n",
                    fr.getMobileNumber(),
                    fr.getTotalCalls(),
                    fr.getDistinctReceivers(),
                    fr.getTotalDuration(),
                    fr.getAvgDuration(),
                    fr.getInternationalCalls(),
                    fr.getNightCalls(),
                    fr.getRiskScore(),
                    fr.getRiskLevel(),
                    fr.getRiskReasons() != null ? fr.getRiskReasons().replace("\"", "'") : ""));
        }

        return sb.toString();
    }
}
