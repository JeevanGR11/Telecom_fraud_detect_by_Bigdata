package com.telecom.controller;

import com.telecom.service.GeoAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/geo")
public class GeoController {

    private final GeoAnalysisService geoAnalysisService;

    public GeoController(GeoAnalysisService geoAnalysisService) {
        this.geoAnalysisService = geoAnalysisService;
    }

    @GetMapping({"", "/analytics", "/summary"})
    public ResponseEntity<Map<String, Object>> getGeoAnalytics() {
        return ResponseEntity.ok(geoAnalysisService.getGeoAnalytics());
    }
}
