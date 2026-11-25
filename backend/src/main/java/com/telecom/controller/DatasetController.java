package com.telecom.controller;

import com.telecom.dto.DatasetDTOs.DatasetGenerateRequest;
import com.telecom.dto.DatasetDTOs.DatasetPreviewDTO;
import com.telecom.entity.DatasetEntity;
import com.telecom.service.DatasetService;
import com.telecom.service.HadoopService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/datasets")
public class DatasetController {

    private final DatasetService datasetService;
    private final HadoopService hadoopService;

    public DatasetController(DatasetService datasetService, HadoopService hadoopService) {
        this.datasetService = datasetService;
        this.hadoopService = hadoopService;
    }

    @PostMapping("/load-sample")
    public ResponseEntity<?> loadSample(
            @RequestParam(value = "force", defaultValue = "false") boolean forceParam,
            @RequestBody(required = false) Map<String, Object> body) {
        boolean force = forceParam || (body != null && Boolean.TRUE.equals(body.get("force")));
        return ResponseEntity.ok(hadoopService.loadSampleCDR(force));
    }

    @GetMapping
    public ResponseEntity<List<DatasetEntity>> getAllDatasets() {
        return ResponseEntity.ok(datasetService.getAllDatasets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DatasetEntity> getDataset(@PathVariable Long id) {
        DatasetEntity entity = datasetService.getDatasetById(id);
        if (entity == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(entity);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadDataset(@RequestParam("file") MultipartFile file,
                                           @RequestParam(value = "name", required = false) String name,
                                           Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "admin";
            DatasetEntity dataset = datasetService.uploadDataset(file, name, username);
            return ResponseEntity.ok(dataset);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateDataset(@RequestBody DatasetGenerateRequest request,
                                             Authentication authentication) {
        int count = request.getCount() > 0 ? request.getCount() : 1000;
        String name = (request.getName() != null && !request.getName().trim().isEmpty())
                ? request.getName().trim() : "Synthetic-Indian-CDR-" + count;

        try {
            String scriptPath = "datasets/generate_datasets.py";
            String outPath = "datasets/synthetic_" + count + "_" + System.currentTimeMillis() + ".csv";

            ProcessBuilder pb = new ProcessBuilder("python", "-c",
                    "import sys; sys.path.append('datasets'); from generate_datasets import generate_cdr; generate_cdr(" + count + ", '" + outPath.replace("\\", "/") + "')");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();

            File generated = new File(outPath);
            if (!generated.exists()) {
                return ResponseEntity.status(500).body(Map.of("error", "Failed to generate dataset file"));
            }

            DatasetEntity entity = new DatasetEntity();
            entity.setName(name);
            entity.setFileName(generated.getName());
            entity.setRecordCount(count);
            entity.setFileSizeKb(Math.round((generated.length() / 1024.0) * 10.0) / 10.0);
            entity.setHdfsPath("/telecom/cdr/input/" + generated.getName());
            entity.setUploadStatus("READY");
            entity.setUploadedBy(authentication != null ? authentication.getName() : "admin");
            entity = datasetService.getAllDatasets().isEmpty() ? entity : entity;

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Generated " + count + " synthetic Indian CDR records successfully.",
                    "filePath", outPath,
                    "datasetName", name
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<DatasetPreviewDTO> previewDataset(@PathVariable Long id) {
        DatasetEntity entity = datasetService.getDatasetById(id);
        if (entity == null) return ResponseEntity.notFound().build();

        File file = new File("datasets/sample-cdr.csv");
        if (!file.exists()) {
            file = new File("datasets/" + entity.getFileName());
        }

        DatasetPreviewDTO preview = datasetService.previewDataset(file);
        return ResponseEntity.ok(preview);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDataset(@PathVariable Long id) {
        datasetService.deleteDataset(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Dataset deleted successfully."));
    }

    @GetMapping("/sample/download")
    public ResponseEntity<Resource> downloadSampleCsv() {
        File file = new File("datasets/sample-cdr.csv");
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"sample-cdr.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
}
