-- ==============================================================
-- TELECOM – Telecommunication Fraud Detection Using Hadoop MapReduce
-- Database: telecom
-- Target RDBMS: MySQL 8.0+ / Spring Data JPA Compatible
-- ==============================================================

CREATE DATABASE IF NOT EXISTS telecom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE telecom;

-- Drop tables in reverse dependency order if recreating
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS threat_records;
DROP TABLE IF EXISTS fraud_results;
DROP TABLE IF EXISTS call_records;
DROP TABLE IF EXISTS analysis_jobs;
DROP TABLE IF EXISTS datasets;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS users;

-- 1. USERS TABLE
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_username (username),
    INDEX idx_user_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. DATASETS TABLE
CREATE TABLE datasets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    record_count INT DEFAULT 0,
    file_size_kb DOUBLE DEFAULT 0.0,
    hdfs_path VARCHAR(255),
    upload_status VARCHAR(30) DEFAULT 'READY',
    uploaded_by VARCHAR(50) DEFAULT 'admin',
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dataset_status (upload_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. CALL_RECORDS TABLE (Indian CDR Records)
CREATE TABLE call_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id VARCHAR(50) NOT NULL,
    caller_number VARCHAR(15) NOT NULL,
    receiver_number VARCHAR(15) NOT NULL,
    call_duration INT NOT NULL DEFAULT 0,
    call_type VARCHAR(20) NOT NULL,
    call_timestamp DATETIME NOT NULL,
    source_region VARCHAR(100),
    destination_region VARCHAR(100),
    ip_address VARCHAR(50),
    network_type VARCHAR(20) DEFAULT '4G VoLTE',
    dataset_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cdr_caller (caller_number),
    INDEX idx_cdr_receiver (receiver_number),
    INDEX idx_cdr_timestamp (call_timestamp),
    INDEX idx_cdr_type (call_type),
    CONSTRAINT fk_cdr_dataset FOREIGN KEY (dataset_id) REFERENCES datasets(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. FRAUD_RESULTS TABLE (Aggregated MapReduce Output & Risk Scores)
CREATE TABLE fraud_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mobile_number VARCHAR(15) NOT NULL UNIQUE,
    total_calls INT NOT NULL DEFAULT 0,
    distinct_receivers INT NOT NULL DEFAULT 0,
    total_duration BIGINT NOT NULL DEFAULT 0,
    avg_duration DOUBLE NOT NULL DEFAULT 0.0,
    international_calls INT NOT NULL DEFAULT 0,
    night_calls INT NOT NULL DEFAULT 0,
    risk_score INT NOT NULL DEFAULT 0,
    risk_level VARCHAR(20) NOT NULL DEFAULT 'LOW RISK',
    risk_reasons TEXT,
    last_job_id VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_fraud_mobile (mobile_number),
    INDEX idx_fraud_risk_score (risk_score),
    INDEX idx_fraud_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. THREAT_RECORDS TABLE (IP Threat Intelligence)
CREATE TABLE threat_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(50) NOT NULL,
    associated_caller VARCHAR(15),
    suspicious_events INT NOT NULL DEFAULT 1,
    threat_level VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    classification VARCHAR(100) NOT NULL,
    activity_details TEXT,
    detected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_threat_ip (ip_address),
    INDEX idx_threat_level (threat_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. ANALYSIS_JOBS TABLE (MapReduce & Pipeline Job Tracking)
CREATE TABLE analysis_jobs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    job_id VARCHAR(50) NOT NULL UNIQUE,
    dataset_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    input_records INT DEFAULT 0,
    processed_records INT DEFAULT 0,
    fraud_records INT DEFAULT 0,
    status VARCHAR(30) NOT NULL DEFAULT 'QUEUED',
    duration_seconds BIGINT DEFAULT 0,
    error_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_job_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. REPORTS TABLE
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    generated_by VARCHAR(50) NOT NULL,
    report_type VARCHAR(50) NOT NULL DEFAULT 'FULL_FRAUD_AUDIT',
    records_analyzed INT DEFAULT 0,
    fraud_cases_found INT DEFAULT 0,
    high_risk_count INT DEFAULT 0,
    medium_risk_count INT DEFAULT 0,
    low_risk_count INT DEFAULT 0,
    file_path VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_type (report_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. AUDIT_LOGS TABLE
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(50),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. SYSTEM_SETTINGS TABLE
CREATE TABLE system_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
