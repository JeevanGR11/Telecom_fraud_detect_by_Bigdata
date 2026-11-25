package com.telecom.controller;

import com.telecom.entity.CallRecord;
import com.telecom.service.CDRService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cdr")
public class CDRController {

    private final CDRService cdrService;

    public CDRController(CDRService cdrService) {
        this.cdrService = cdrService;
    }

    @GetMapping({"", "/records"})
    public ResponseEntity<Page<CallRecord>> getRecords(
            @RequestParam(required = false) String caller,
            @RequestParam(required = false) String receiver,
            @RequestParam(required = false) String callType,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(defaultValue = "callTimestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Page<CallRecord> records = cdrService.getRecords(caller, receiver, callType, startDate, endDate, page, size, sortBy, direction);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<CallRecord> getRecordById(@PathVariable Long id) {
        CallRecord record = cdrService.getRecordById(id);
        if (record == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(record);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportCsv() {
        String csvData = cdrService.exportAllToCsv();
        byte[] bytes = csvData.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cdr_export_" + System.currentTimeMillis() + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadCdrFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please select a non-empty CSV file."));
        }
        try {
            int count = cdrService.importRecordsFromCsv(file.getInputStream(), null);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("importedRecords", count);
            resp.put("message", "Successfully imported " + count + " CDR records.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
