package com.telecom.controller;

import com.telecom.entity.ThreatRecord;
import com.telecom.service.IPThreatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/threats")
public class ThreatController {

    private final IPThreatService threatService;

    public ThreatController(IPThreatService threatService) {
        this.threatService = threatService;
    }

    @GetMapping
    public ResponseEntity<List<ThreatRecord>> getAllThreats() {
        return ResponseEntity.ok(threatService.getAllThreats());
    }
}
