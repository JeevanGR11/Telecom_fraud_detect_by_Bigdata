package com.telecom.controller;

import com.telecom.dto.HadoopDTOs.HadoopStatusDTO;
import com.telecom.dto.HadoopDTOs.JobStatusDTO;
import com.telecom.dto.HadoopDTOs.JobTriggerRequest;
import com.telecom.entity.AnalysisJob;
import com.telecom.repository.AnalysisJobRepository;
import com.telecom.service.HadoopService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hadoop")
public class HadoopController {

    private final HadoopService hadoopService;
    private final AnalysisJobRepository analysisJobRepository;

    public HadoopController(HadoopService hadoopService, AnalysisJobRepository analysisJobRepository) {
        this.hadoopService = hadoopService;
        this.analysisJobRepository = analysisJobRepository;
    }

    @GetMapping("/status")
    public ResponseEntity<HadoopStatusDTO> getStatus() {
        return ResponseEntity.ok(hadoopService.getHadoopStatus());
    }

    @PostMapping("/load-sample")
    public ResponseEntity<?> loadSample(
            @RequestParam(value = "force", defaultValue = "false") boolean forceParam,
            @RequestBody(required = false) Map<String, Object> body) {
        boolean force = forceParam || (body != null && Boolean.TRUE.equals(body.get("force")));
        return ResponseEntity.ok(hadoopService.loadSampleCDR(force));
    }

    @PostMapping("/start")
    public ResponseEntity<JobStatusDTO> startJob(@RequestBody(required = false) JobTriggerRequest request) {
        if (request == null) {
            request = new JobTriggerRequest();
        }
        return ResponseEntity.ok(hadoopService.startMapReduceJob(request));
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<AnalysisJob>> getJobs() {
        return ResponseEntity.ok(analysisJobRepository.findByOrderByStartTimeDesc());
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<AnalysisJob> getJobDetails(@PathVariable String jobId) {
        return analysisJobRepository.findByJobId(jobId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
