-- ==============================================================
-- TELECOM – Sample Data & Seed Initialization
-- Database: telecom
-- ==============================================================

USE telecom;

-- 1. SEED USERS
-- Password for 'admin': admin123
-- Password for 'demo': demo123
INSERT INTO users (username, email, password_hash, full_name, role, status) VALUES
('admin', 'admin@telecom.in', '$2a$10$XSrDkH2h//C.uc59llZmaufErYJtpJoIvd1jqIriumC3g07q6zv/C', 'System Administrator', 'ADMIN', 'ACTIVE'),
('demo', 'demo@telecom.in', '$2a$10$6X/TEI02i4O1FWSLmb9pLuafLLZ2JqVnuecueUCVluZs8jcuiaU0a', 'Demo Investigator', 'USER', 'ACTIVE'),
('rajesh.k', 'rajesh.k@telecom.in', '$2a$10$6X/TEI02i4O1FWSLmb9pLuafLLZ2JqVnuecueUCVluZs8jcuiaU0a', 'Rajesh Kumar', 'USER', 'ACTIVE'),
('priya.sharma', 'priya.s@telecom.in', '$2a$10$6X/TEI02i4O1FWSLmb9pLuafLLZ2JqVnuecueUCVluZs8jcuiaU0a', 'Priya Sharma', 'USER', 'ACTIVE');

-- 2. SYSTEM SETTINGS
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('fraud.threshold.frequency', '8', 'Call frequency threshold for triggering volume alert'),
('fraud.threshold.distinct.receivers', '6', 'Threshold of distinct receivers before triggering blast alert'),
('fraud.threshold.wangiri.avg.duration', '5', 'Max average call duration in seconds for Wangiri detection'),
('fraud.threshold.excessive.duration', '3600', 'Total duration in seconds to trigger excessive usage alert'),
('fraud.threshold.night.ratio', '0.35', 'Night calls proportion (00:00 - 05:00) to trigger alert'),
('hadoop.hdfs.input.path', '/telecom/cdr/input', 'Default HDFS directory for CDR dataset uploads'),
('hadoop.hdfs.output.path', '/telecom/cdr/output', 'Default HDFS output directory for MapReduce results'),
('hadoop.namenode.url', 'hdfs://localhost:9000', 'Active Hadoop HDFS NameNode URL');

-- 3. INITIAL DATASET RECORD
INSERT INTO datasets (name, file_name, record_count, file_size_kb, hdfs_path, upload_status, uploaded_by) VALUES
('Indian-Telecom-CDR-Primary', 'indian_cdr_primary_10k.csv', 1000, 84.5, '/telecom/cdr/input/indian_cdr_primary_10k.csv', 'PROCESSED', 'admin'),
('Bengaluru-Metro-Synthetic', 'bengaluru_metro_cdr.csv', 500, 42.1, '/telecom/cdr/input/bengaluru_metro_cdr.csv', 'READY', 'demo');

-- 4. SEED CALL DETAIL RECORDS (INDIAN CDR)
INSERT INTO call_records (record_id, caller_number, receiver_number, call_duration, call_type, call_timestamp, source_region, destination_region, ip_address, network_type, dataset_id) VALUES
('CDR-10001', '9876543210', '9845012345', 3, 'OUTGOING', '2026-11-20 01:14:22', 'Bengaluru, Karnataka', 'Chennai, Tamil Nadu', '103.24.18.52', '5G VoNR', 1),
('CDR-10002', '9876543210', '8765432109', 4, 'OUTGOING', '2026-11-20 01:16:05', 'Bengaluru, Karnataka', 'Hyderabad, Telangana', '103.24.18.52', '5G VoNR', 1),
('CDR-10003', '9876543210', '9123456780', 2, 'OUTGOING', '2026-11-20 01:18:40', 'Bengaluru, Karnataka', 'Mumbai, Maharashtra', '103.24.18.52', '5G VoNR', 1),
('CDR-10004', '9876543210', '9988776655', 4, 'OUTGOING', '2026-11-20 01:21:12', 'Bengaluru, Karnataka', 'Delhi', '103.24.18.52', '5G VoNR', 1),
('CDR-10005', '9876543210', '9876501234', 3, 'OUTGOING', '2026-11-20 01:23:45', 'Bengaluru, Karnataka', 'Pune, Maharashtra', '103.24.18.52', '5G VoNR', 1),
('CDR-10006', '9876543210', '9845123456', 5, 'OUTGOING', '2026-11-20 01:25:30', 'Bengaluru, Karnataka', 'Kolkata, West Bengal', '103.24.18.52', '5G VoNR', 1),
('CDR-10007', '9876543210', '8765123490', 2, 'OUTGOING', '2026-11-20 01:27:18', 'Bengaluru, Karnataka', 'Ahmedabad, Gujarat', '103.24.18.52', '5G VoNR', 1),
('CDR-10008', '9876543210', '9123456789', 3, 'OUTGOING', '2026-11-20 01:30:02', 'Bengaluru, Karnataka', 'Mysuru, Karnataka', '103.24.18.52', '5G VoNR', 1),
('CDR-10009', '9876543210', '9988771234', 4, 'OUTGOING', '2026-11-20 01:33:19', 'Bengaluru, Karnataka', 'Kochi, Kerala', '103.24.18.52', '5G VoNR', 1),
('CDR-10010', '9876543210', '9740123456', 2, 'OUTGOING', '2026-11-20 01:36:50', 'Bengaluru, Karnataka', 'Bengaluru, Karnataka', '103.24.18.52', '5G VoNR', 1),

-- High volume / Robocall suspect
('CDR-10011', '8765432109', '9845012345', 18, 'OUTGOING', '2026-11-21 14:02:11', 'Delhi', 'Bengaluru, Karnataka', '49.207.12.90', '4G VoLTE', 1),
('CDR-10012', '8765432109', '9123456780', 22, 'OUTGOING', '2026-11-21 14:04:30', 'Delhi', 'Mumbai, Maharashtra', '49.207.12.90', '4G VoLTE', 1),
('CDR-10013', '8765432109', '9988776655', 15, 'OUTGOING', '2026-11-21 14:07:05', 'Delhi', 'Chennai, Tamil Nadu', '49.207.12.90', '4G VoLTE', 1),
('CDR-10014', '8765432109', '9876501234', 19, 'OUTGOING', '2026-11-21 14:09:44', 'Delhi', 'Hyderabad, Telangana', '49.207.12.90', '4G VoLTE', 1),
('CDR-10015', '8765432109', '9845123456', 25, 'OUTGOING', '2026-11-21 14:12:18', 'Delhi', 'Kolkata, West Bengal', '49.207.12.90', '4G VoLTE', 1),
('CDR-10016', '8765432109', '8765123490', 17, 'OUTGOING', '2026-11-21 14:15:22', 'Delhi', 'Ahmedabad, Gujarat', '49.207.12.90', '4G VoLTE', 1),
('CDR-10017', '8765432109', '9123456789', 30, 'OUTGOING', '2026-11-21 14:18:01', 'Delhi', 'Pune, Maharashtra', '49.207.12.90', '4G VoLTE', 1),
('CDR-10018', '8765432109', '9988771234', 21, 'OUTGOING', '2026-11-21 14:21:40', 'Delhi', 'Mysuru, Karnataka', '49.207.12.90', '4G VoLTE', 1),

-- International toll bypass / Conference fraud suspect
('CDR-10019', '9123456780', '9845012345', 1420, 'INTERNATIONAL', '2026-11-22 03:10:00', 'Mumbai, Maharashtra', 'London, UK', '122.161.45.10', '5G VoNR', 1),
('CDR-10020', '9123456780', '8765432109', 1850, 'INTERNATIONAL', '2026-11-22 03:35:12', 'Mumbai, Maharashtra', 'Dubai, UAE', '122.161.45.10', '5G VoNR', 1),
('CDR-10021', '9123456780', '9988776655', 960, 'INTERNATIONAL', '2026-11-22 04:08:44', 'Mumbai, Maharashtra', 'Singapore', '122.161.45.10', '5G VoNR', 1),

-- Normal subscriber calls
('CDR-10022', '9845012345', '9876543210', 145, 'INCOMING', '2026-11-22 10:15:30', 'Chennai, Tamil Nadu', 'Bengaluru, Karnataka', '106.51.22.88', '4G VoLTE', 1),
('CDR-10023', '9845012345', '9988776655', 210, 'OUTGOING', '2026-11-22 11:40:12', 'Chennai, Tamil Nadu', 'Bengaluru, Karnataka', '106.51.22.88', '4G VoLTE', 1),
('CDR-10024', '9845012345', '9876501234', 180, 'OUTGOING', '2026-11-22 16:25:00', 'Chennai, Tamil Nadu', 'Mysuru, Karnataka', '106.51.22.88', '4G VoLTE', 1),
('CDR-10025', '9988776655', '9845012345', 310, 'INCOMING', '2026-11-22 14:05:18', 'Hyderabad, Telangana', 'Chennai, Tamil Nadu', '115.240.10.14', '5G VoNR', 1);

-- 5. FRAUD RESULTS (Hadoop MapReduce Aggregated Analytics)
INSERT INTO fraud_results (mobile_number, total_calls, distinct_receivers, total_duration, avg_duration, international_calls, night_calls, risk_score, risk_level, risk_reasons, last_job_id) VALUES
('9876543210', 10, 10, 32, 3.20, 0, 10, 85, 'HIGH RISK', 'Abnormally high calling volume (10 calls) | Excessive distinct receivers (10 targets) | Suspected Wangiri / Ping-Call pattern (avg 3.2s) | Unusual night call ratio (100.0%)', 'JOB_HADOOP_20261120_001'),
('8765432109', 8, 8, 177, 22.13, 0, 0, 70, 'HIGH RISK', 'Elevated call frequency (8 calls) | High distinct target count (8 targets)', 'JOB_HADOOP_20261120_001'),
('9123456780', 3, 3, 4230, 1410.00, 3, 3, 60, 'MEDIUM RISK', 'Excessive total duration (70 minutes) | Unusual night call ratio (100.0%) | Frequent international dialing (3 ISD calls)', 'JOB_HADOOP_20261120_001'),
('9845012345', 3, 3, 535, 178.33, 0, 0, 10, 'NORMAL', 'Normal Subscriber Activity', 'JOB_HADOOP_20261120_001'),
('9988776655', 1, 1, 310, 310.00, 0, 0, 5, 'NORMAL', 'Normal Subscriber Activity', 'JOB_HADOOP_20261120_001');

-- 6. THREAT RECORDS (IP Threat Intelligence)
INSERT INTO threat_records (ip_address, associated_caller, suspicious_events, threat_level, classification, activity_details) VALUES
('103.24.18.52', '9876543210', 10, 'CRITICAL', 'Wangiri / Automated SIM Box Blast', 'Repeated short duration calls initiated between 01:00 and 02:00 AM across 10 distinct Indian mobile circles.'),
('49.207.12.90', '8765432109', 8, 'HIGH', 'Robocall Telemarketing Spammer', 'High-frequency sequential outbound calls generated through automated dialing gateway.'),
('122.161.45.10', '9123456780', 3, 'HIGH', 'International Toll Bypass / PBX Hacking', 'Continuous multi-hour international destination routing observed during late night off-peak window.'),
('182.72.68.14', '9876501234', 4, 'MEDIUM', 'Suspicious SIP Endpoint Activity', 'Intermittent rapid connection re-attempts detected across short intervals.');

-- 7. ANALYSIS JOBS (MapReduce Execution History)
INSERT INTO analysis_jobs (job_id, dataset_name, start_time, end_time, input_records, processed_records, fraud_records, status, duration_seconds) VALUES
('JOB_HADOOP_20261120_001', 'Indian-Telecom-CDR-Primary', '2026-11-20 02:00:15', '2026-11-20 02:01:03', 1000, 998, 2, 'COMPLETED', 48),
('JOB_HADOOP_20261121_002', 'Bengaluru-Metro-Synthetic', '2026-11-21 15:30:00', '2026-11-21 15:30:32', 500, 500, 1, 'COMPLETED', 32);

-- 8. REPORTS
INSERT INTO reports (title, generated_by, report_type, records_analyzed, fraud_cases_found, high_risk_count, medium_risk_count, low_risk_count, file_path) VALUES
('Weekly Telecom Fraud Executive Summary - Nov 2026', 'admin', 'EXECUTIVE_SUMMARY', 1500, 3, 2, 1, 0, '/reports/telecom_fraud_weekly_nov2026.pdf'),
('Wangiri & SIM Box Anomaly Audit Report', 'admin', 'ANOMALY_AUDIT', 1000, 2, 2, 0, 0, '/reports/wangiri_audit_nov2026.pdf');

-- 9. AUDIT LOGS
INSERT INTO audit_logs (username, action, details, ip_address) VALUES
('admin', 'SYSTEM_BOOT', 'TELECOM fraud detection platform initialized successfully', '127.0.0.1'),
('admin', 'HADOOP_JOB_SUBMIT', 'Submitted MapReduce job JOB_HADOOP_20261120_001 on Indian-Telecom-CDR-Primary', '127.0.0.1'),
('demo', 'SEARCH_INVESTIGATION', 'Searched Indian mobile number 9876543210 (Risk Score: 85)', '127.0.0.1');
