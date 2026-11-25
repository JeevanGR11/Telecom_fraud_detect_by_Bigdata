package com.telecom.service;

import com.telecom.dto.DatasetDTOs.DatasetPreviewDTO;
import com.telecom.entity.DatasetEntity;
import com.telecom.repository.DatasetRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final CDRService cdrService;

    public DatasetService(DatasetRepository datasetRepository, CDRService cdrService) {
        this.datasetRepository = datasetRepository;
        this.cdrService = cdrService;
    }

    public List<DatasetEntity> getAllDatasets() {
        return datasetRepository.findByOrderByUploadedAtDesc();
    }

    public DatasetEntity getDatasetById(Long id) {
        return datasetRepository.findById(id).orElse(null);
    }

    public DatasetEntity uploadDataset(MultipartFile file, String name, String username) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_cdr.csv";
        String datasetName = (name != null && !name.trim().isEmpty()) ? name.trim() : fileName.replace(".csv", "");

        File saveDir = new File("datasets/uploads");
        if (!saveDir.exists()) saveDir.mkdirs();
        File targetFile = new File(saveDir, System.currentTimeMillis() + "_" + fileName);
        file.transferTo(targetFile);

        DatasetEntity entity = new DatasetEntity();
        entity.setName(datasetName);
        entity.setFileName(fileName);
        entity.setFileSizeKb(Math.round((file.getSize() / 1024.0) * 10.0) / 10.0);
        entity.setHdfsPath("/telecom/cdr/input/" + fileName);
        entity.setUploadedBy(username != null ? username : "admin");
        entity.setUploadStatus("READY");
        entity.setUploadedAt(LocalDateTime.now());
        entity = datasetRepository.save(entity);

        // Import records into database
        try (InputStream is = new FileInputStream(targetFile)) {
            int imported = cdrService.importRecordsFromCsv(is, entity.getId());
            entity.setRecordCount(imported);
            entity.setUploadStatus("PROCESSED");
            datasetRepository.save(entity);
        } catch (Exception e) {
            entity.setUploadStatus("ERROR");
            datasetRepository.save(entity);
            throw e;
        }

        return entity;
    }

    public DatasetPreviewDTO previewDataset(File file) {
        DatasetPreviewDTO preview = new DatasetPreviewDTO();
        List<List<String>> sampleRows = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (!file.exists()) {
            preview.setValid(false);
            errors.add("File not found: " + file.getPath());
            preview.setValidationErrors(errors);
            return preview;
        }

        preview.setName(file.getName());
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line == null) {
                preview.setValid(false);
                errors.add("CSV file is completely empty");
                preview.setValidationErrors(errors);
                return preview;
            }

            if (line.startsWith("\uFEFF")) line = line.substring(1).trim();
            String[] headers = line.split(",", -1);
            preview.setHeaders(Arrays.asList(headers));

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                count++;

                String[] row = line.split(",", -1);
                if (sampleRows.size() < 10) {
                    sampleRows.add(Arrays.asList(row));
                }

                // Basic validation checks
                if (row.length < 5) {
                    if (errors.size() < 5) errors.add("Row " + count + " has fewer than required columns (" + row.length + ")");
                }
            }

            preview.setTotalRecords(count);
            preview.setSampleRows(sampleRows);
            preview.setValid(errors.isEmpty());
            preview.setValidationErrors(errors);

        } catch (Exception e) {
            preview.setValid(false);
            errors.add("Error parsing CSV: " + e.getMessage());
            preview.setValidationErrors(errors);
        }

        return preview;
    }

    public void deleteDataset(Long id) {
        datasetRepository.deleteById(id);
    }
}
