package com.telecom.controller;

import com.telecom.dto.ClusteringDTOs.ClusterGroupDTO;
import com.telecom.service.AIClusteringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clustering")
public class ClusteringController {

    private final AIClusteringService clusteringService;

    public ClusteringController(AIClusteringService clusteringService) {
        this.clusteringService = clusteringService;
    }

    @GetMapping({"", "/results", "/summary", "/analytics"})
    public ResponseEntity<List<ClusterGroupDTO>> getClusters() {
        return ResponseEntity.ok(clusteringService.performClustering());
    }
}
