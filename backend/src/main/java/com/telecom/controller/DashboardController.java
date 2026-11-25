package com.telecom.controller;

import com.telecom.dto.DashboardDTOs.*;
import com.telecom.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/call-volume")
    public ResponseEntity<ChartDataDTO> getCallVolume(@RequestParam(defaultValue = "daily") String period) {
        return ResponseEntity.ok(dashboardService.getCallVolume(period));
    }

    @GetMapping("/fraud-trends")
    public ResponseEntity<ChartDataDTO> getFraudTrends() {
        return ResponseEntity.ok(dashboardService.getFraudTrends());
    }

    @GetMapping("/risk-distribution")
    public ResponseEntity<ChartDataDTO> getRiskDistribution() {
        return ResponseEntity.ok(dashboardService.getRiskDistribution());
    }

    @GetMapping("/call-types")
    public ResponseEntity<ChartDataDTO> getCallTypeDistribution() {
        return ResponseEntity.ok(dashboardService.getCallTypeDistribution());
    }

    @GetMapping("/recent-alerts")
    public ResponseEntity<List<AlertItemDTO>> getRecentAlerts() {
        return ResponseEntity.ok(dashboardService.getRecentAlerts());
    }

    @GetMapping("/top-suspicious")
    public ResponseEntity<List<Map<String, Object>>> getTopSuspiciousNumbers() {
        return ResponseEntity.ok(dashboardService.getTopSuspiciousNumbers());
    }
}
