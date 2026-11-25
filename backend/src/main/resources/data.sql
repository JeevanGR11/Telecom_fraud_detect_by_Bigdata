-- ==============================================================
-- TELECOM – Spring Boot Automatic Seeding (data.sql)
-- ==============================================================

-- 1. Users (demo / demo123, admin / admin123)
MERGE INTO users (id, username, email, password_hash, full_name, role, status) KEY(username) VALUES
(1, 'admin', 'admin@telecom.in', '$2a$10$XSrDkH2h//C.uc59llZmaufErYJtpJoIvd1jqIriumC3g07q6zv/C', 'System Administrator', 'ADMIN', 'ACTIVE'),
(2, 'demo', 'demo@telecom.in', '$2a$10$6X/TEI02i4O1FWSLmb9pLuafLLZ2JqVnuecueUCVluZs8jcuiaU0a', 'Demo Investigator', 'USER', 'ACTIVE'),
(3, 'rajesh.k', 'rajesh.k@telecom.in', '$2a$10$6X/TEI02i4O1FWSLmb9pLuafLLZ2JqVnuecueUCVluZs8jcuiaU0a', 'Rajesh Kumar', 'USER', 'ACTIVE');

-- 2. System Settings
MERGE INTO system_settings (id, setting_key, setting_value, description) KEY(setting_key) VALUES
(1, 'fraud.threshold.frequency', '8', 'Call frequency threshold for triggering volume alert'),
(2, 'fraud.threshold.distinct.receivers', '6', 'Threshold of distinct receivers before triggering blast alert'),
(3, 'fraud.threshold.wangiri.avg.duration', '5', 'Max average call duration in seconds for Wangiri detection'),
(4, 'fraud.threshold.excessive.duration', '3600', 'Total duration in seconds to trigger excessive usage alert'),
(5, 'fraud.threshold.night.ratio', '0.35', 'Night calls proportion (00:00 - 05:00) to trigger alert'),
(6, 'hadoop.hdfs.input.path', '/telecom/cdr/input', 'Default HDFS directory for CDR dataset uploads'),
(7, 'hadoop.hdfs.output.path', '/telecom/cdr/output', 'Default HDFS output directory for MapReduce results'),
(8, 'hadoop.namenode.url', 'hdfs://localhost:9000', 'Active Hadoop HDFS NameNode URL');

-- 3. Initial Datasets
MERGE INTO datasets (id, name, file_name, record_count, file_size_kb, hdfs_path, upload_status, uploaded_by) KEY(name) VALUES
(1, 'Indian-Telecom-CDR-Primary', 'sample-cdr.csv', 1000, 84.5, '/telecom/cdr/input/sample-cdr.csv', 'PROCESSED', 'admin'),
(2, 'Bengaluru-Metro-Synthetic', 'bengaluru_metro_cdr.csv', 500, 42.1, '/telecom/cdr/input/bengaluru_metro_cdr.csv', 'READY', 'demo');

-- 4. Initial Fraud Results
MERGE INTO fraud_results (id, mobile_number, total_calls, distinct_receivers, total_duration, avg_duration, international_calls, night_calls, risk_score, risk_level, risk_reasons, last_job_id) KEY(mobile_number) VALUES
(1, '9876543210', 25, 23, 78, 3.12, 0, 25, 85, 'HIGH RISK', 'Abnormally high calling volume (25 calls) | Excessive distinct receivers (23 targets) | Suspected Wangiri / Ping-Call pattern (avg 3.1s) | Unusual night call ratio (100.0%)', 'JOB_HADOOP_20261120_001'),
(2, '8765432109', 30, 28, 642, 21.40, 0, 0, 70, 'HIGH RISK', 'Abnormally high calling volume (30 calls) | Excessive distinct receivers (28 targets)', 'JOB_HADOOP_20261120_001'),
(3, '9123456780', 8, 8, 18450, 2306.25, 8, 8, 60, 'MEDIUM RISK', 'Excessive total duration (307 minutes) | Unusual night call ratio (100.0%) | Frequent international dialing (8 ISD calls)', 'JOB_HADOOP_20261120_001'),
(4, '9845012345', 4, 3, 620, 155.00, 0, 0, 10, 'NORMAL', 'Normal Subscriber Activity', 'JOB_HADOOP_20261120_001'),
(5, '9988776655', 3, 2, 450, 150.00, 0, 0, 5, 'NORMAL', 'Normal Subscriber Activity', 'JOB_HADOOP_20261120_001');

-- 5. Threat Records
MERGE INTO threat_records (id, ip_address, associated_caller, suspicious_events, threat_level, classification, activity_details) KEY(ip_address) VALUES
(1, '103.24.18.52', '9876543210', 25, 'CRITICAL', 'Wangiri / Automated SIM Box Blast', 'Repeated short duration calls initiated between 01:00 and 04:00 AM across 23 distinct Indian mobile circles.'),
(2, '49.207.12.90', '8765432109', 30, 'HIGH', 'Robocall Telemarketing Spammer', 'High-frequency sequential outbound calls generated through automated dialing gateway.'),
(3, '122.161.45.10', '9123456780', 8, 'HIGH', 'International Toll Bypass / PBX Hacking', 'Continuous multi-hour international destination routing observed during late night off-peak window.'),
(4, '182.72.68.14', '9876501234', 4, 'MEDIUM', 'Suspicious SIP Endpoint Activity', 'Intermittent rapid connection re-attempts detected across short intervals.');

-- 6. Analysis Jobs
MERGE INTO analysis_jobs (id, job_id, dataset_name, start_time, end_time, input_records, processed_records, fraud_records, status, duration_seconds) KEY(job_id) VALUES
(1, 'JOB_HADOOP_20261120_001', 'Indian-Telecom-CDR-Primary', '2026-11-20 02:00:15', '2026-11-20 02:01:03', 1000, 1000, 3, 'COMPLETED', 48),
(2, 'JOB_HADOOP_20261121_002', 'Bengaluru-Metro-Synthetic', '2026-11-21 15:30:00', '2026-11-21 15:30:32', 500, 500, 1, 'COMPLETED', 32);

-- 7. Reports
MERGE INTO reports (id, title, generated_by, report_type, records_analyzed, fraud_cases_found, high_risk_count, medium_risk_count, low_risk_count, file_path) KEY(title) VALUES
(1, 'Weekly Telecom Fraud Executive Summary - Nov 2026', 'admin', 'EXECUTIVE_SUMMARY', 1500, 3, 2, 1, 0, '/reports/telecom_fraud_weekly_nov2026.pdf'),
(2, 'Wangiri & SIM Box Anomaly Audit Report', 'admin', 'ANOMALY_AUDIT', 1000, 2, 2, 0, 0, '/reports/wangiri_audit_nov2026.pdf');

-- 8. Audit Logs
INSERT INTO audit_logs (username, action, details, ip_address) VALUES
('admin', 'SYSTEM_BOOT', 'TELECOM fraud detection platform initialized successfully', '127.0.0.1');
