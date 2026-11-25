package com.telecom.controller;

import com.telecom.dto.ReportDTOs.ReportGenerateRequest;
import com.telecom.entity.ReportEntity;
import com.telecom.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ResponseEntity<List<ReportEntity>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @PostMapping("/generate")
    public ResponseEntity<ReportEntity> generateReport(@RequestBody ReportGenerateRequest request,
                                                       Authentication authentication) {
        String username = authentication != null ? authentication.getName() : "admin";
        ReportEntity report = reportService.generateReport(request, username);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadReport(@PathVariable Long id) {
        String csv = reportService.exportReportCsv(id);
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"telecom_fraud_report_" + id + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }
}
