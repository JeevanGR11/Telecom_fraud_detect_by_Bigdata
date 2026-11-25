package com.telecom.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "datasets")
public class DatasetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "record_count")
    private Integer recordCount = 0;

    @Column(name = "file_size_kb")
    private Double fileSizeKb = 0.0;

    @Column(name = "hdfs_path")
    private String hdfsPath;

    @Column(name = "upload_status", length = 30)
    private String uploadStatus = "READY"; // READY, UPLOADING, PROCESSING, PROCESSED, ERROR

    @Column(name = "uploaded_by", length = 50)
    private String uploadedBy = "admin";

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public DatasetEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public Integer getRecordCount() { return recordCount; }
    public void setRecordCount(Integer recordCount) { this.recordCount = recordCount; }

    public Double getFileSizeKb() { return fileSizeKb; }
    public void setFileSizeKb(Double fileSizeKb) { this.fileSizeKb = fileSizeKb; }

    public String getHdfsPath() { return hdfsPath; }
    public void setHdfsPath(String hdfsPath) { this.hdfsPath = hdfsPath; }

    public String getUploadStatus() { return uploadStatus; }
    public void setUploadStatus(String uploadStatus) { this.uploadStatus = uploadStatus; }

    public String getUploadedBy() { return uploadedBy; }
    public void setUploadedBy(String uploadedBy) { this.uploadedBy = uploadedBy; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
