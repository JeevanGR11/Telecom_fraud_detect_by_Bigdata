package com.telecom.controller;

import com.telecom.dto.HadoopDTOs.JobStatusDTO;
import com.telecom.dto.HadoopDTOs.JobTriggerRequest;
import com.telecom.dto.SearchFraudDTO;
import com.telecom.entity.FraudResult;
import com.telecom.service.FraudDetectionService;
import com.telecom.service.HadoopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
public class FraudController {

    private final FraudDetectionService fraudDetectionService;
    private final HadoopService hadoopService;

    public FraudController(FraudDetectionService fraudDetectionService,
                           HadoopService hadoopService) {
        this.fraudDetectionService = fraudDetectionService;
        this.hadoopService = hadoopService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<JobStatusDTO> startAnalysis(@RequestBody(required = false) JobTriggerRequest request) {
        if (request == null) {
            request = new JobTriggerRequest();
        }
        JobStatusDTO result = hadoopService.startMapReduceJob(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/search/{mobileNumber}")
    public ResponseEntity<SearchFraudDTO> searchMobile(@PathVariable String mobileNumber) {
        SearchFraudDTO result = fraudDetectionService.searchNumber(mobileNumber);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/results")
    public ResponseEntity<List<FraudResult>> getAllResults() {
        return ResponseEntity.ok(fraudDetectionService.getAllFraudResults());
    }
}
