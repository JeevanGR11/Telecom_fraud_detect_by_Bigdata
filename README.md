# TELECOM – Telecommunication Fraud Detection Using Hadoop MapReduce

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Apache Hadoop](https://img.shields.io/badge/Apache%20Hadoop-3.3.6-orange.svg)](https://hadoop.apache.org/)
[![Java](https://img.shields.io/badge/Java-17%20%7C%2026-blue.svg)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-Academic%20BDT-lightgrey.svg)]()
[![Theme](https://img.shields.io/badge/UI%20Theme-Light%20SaaS-red.svg)]()

> **Big Data Technology (BDT) Project**  
> An enterprise-grade, distributed telecommunications fraud analytics and monitoring platform engineered with **Apache Hadoop MapReduce**, **Spring Boot 3**, **Spring Security (JWT + BCrypt)**, and a **Light-Theme Responsive SaaS Dashboard**.

---

## 1. Project Overview

Modern telecommunications carriers process billions of Call Detail Records (CDRs) every 24 hours. Fraudulent calling behaviors cause billions of dollars in annual revenue leakage and disrupt subscriber quality of service.

**TELECOM** leverages distributed big data processing (MapReduce) to analyze large volumes of Indian telecommunications CDR datasets, identify suspicious calling patterns, and calculate real-time fraud risk scores without claiming definitive guilt.

### Key Fraud Patterns Detected:
1. **Wangiri / Ping-Call Fraud**: Automated burst dialing with ultra-short durations (<5 seconds) designed to induce expensive callbacks to premium international rate numbers.
2. **Robocall / Telemarketing Spam**: High-frequency outbound blast dialing targeting dozens of distinct recipients across rapid intervals.
3. **International Toll Bypass / PBX Hacking**: Sustained multi-hour international calls routed during off-peak night windows to bypass carrier tariffs.
4. **SIM Box / Gateway Threat Activity**: High-density burst traffic from unverified VoIP gateways and spoofed IP endpoints.

---

## 2. System Architecture

```
                                    +-----------------------------------------+
                                    |         TELECOM Web Dashboard           |
                                    |   (Light SaaS Theme: #FFFFFF / #E63946) |
                                    +--------------------+--------------------+
                                                         | (REST / JSON + JWT)
                                                         v
                                    +-----------------------------------------+
                                    |     Spring Boot 3 Application Layer     |
                                    |  - JWT Stateless Auth & Spring Security |
                                    |  - Indian Phone Normalization & Rules   |
                                    |  - Analytics & AI Clustering Engine     |
                                    +-----+-----------------------------+-----+
                                          |                             |
                       (JPA / Hibernate)  v                             v  (HDFS / MapReduce)
                      +----------------------+               +----------------------+
                      | Persistent Store     |               | Distributed Cluster  |
                      | - MySQL 8 (Prod)     |               | - Hadoop HDFS 3.3.6  |
                      | - H2 File (Embedded) |               | - MapReduce Drivers  |
                      +----------------------+               +----------------------+
```

### Complete Layer Breakdown:
- **Presentation Layer**: 15 responsive HTML5 views built with Bootstrap 5.3, Bootstrap Icons, and Chart.js 4.4 following a strict light color palette (`#FFFFFF` background, `#F8FAFC` secondary, `#E63946` brand red).
- **Backend Service Layer (`com.telecom`)**: Spring Boot 3 with 12 REST controllers, 12 business services, and 9 Spring Data JPA repositories.
- **Big Data Engine (`hadoop/`)**: Apache Hadoop 3.3.6 MapReduce custom mappers, reducers, and writable data carriers (`CDRFraudMapper`, `CDRFraudReducer`, `CDRRecordWritable`).
- **Persistence Store**: MySQL 8.0 target with zero-configuration persistent embedded H2 fallback (`data/telecomdb.mv.db`).

---

## 3. UI/UX Design System (Light Theme)

The interface strictly adopts a clean, high-contrast, modern **Light Theme** across every view:

- **Primary Background**: `#FFFFFF` (Crisp white)
- **Secondary Background**: `#F8FAFC` (Subtle off-white/grey)
- **Card & Sidebar Background**: `#FFFFFF`
- **Primary Brand Color**: `#E63946` (Telecom vibrant red)
- **Primary Typography**: `#1E293B` (Slate dark)
- **Secondary Typography**: `#64748B` (Muted slate)
- **Border Accents**: `#E2E8F0`
- **Risk Badges**:
  - `0 - 39`: Green `#16A34A` (LOW RISK / NORMAL)
  - `40 - 69`: Amber `#F59E0B` (MEDIUM RISK / SUSPICIOUS)
  - `70 - 100`: Red `#DC2626` (HIGH RISK / ANOMALOUS)

---

## 4. Feature Matrix & Module Map

| # | Navigation Module | Endpoint / View | Key Features |
|---|---|---|---|
| 1 | **Login** | `/login.html` | Branded interface, toggle password, remember me, demo credentials autofill. |
| 2 | **Registration** | `/register.html` | Real-time input validation, password strength meter, email checks, role selection. |
| 3 | **Dashboard** | `/dashboard.html` | 4 KPI cards (Total Calls, Analyzed Calls, Fraud Detected, Rate), 5 Chart.js charts, recent alerts feed. |
| 4 | **Search Fraud** | `/search-fraud.html` | Indian 10-digit number investigator, risk score dial, call history table, hourly volume chart. |
| 5 | **Call Records** | `/call-records.html` | Full CDR browser, multi-filter query (caller, receiver, type, dates), pagination, CSV download. |
| 6 | **Fraud Detection** | `/fraud-detection.html` | Hadoop MapReduce trigger console, threshold customization, pipeline stage indicator. |
| 7 | **IP Threats** | `/ip-threats.html` | Gateway IP monitoring, threat classification badges, associated phone numbers. |
| 8 | **Geo Analysis** | `/geo-analysis.html` | Geographic call density across 14 Indian telecom circles and state hotspots. |
| 9 | **AI Clustering** | `/ai-clustering.html` | K-Means behavioral clustering (Wangiri, Robocallers, Toll Bypass, Benign Subscribers). |
| 10 | **Reports** | `/reports.html` | Audit report generator, executive summaries, printable audit modal, CSV export. |
| 11 | **Dataset Management**| `/datasets.html` | Upload new CDR CSVs with schema validator, synthetic CDR generator (1K, 10K, 100K). |
| 12 | **Analysis History** | `/analysis-history.html` | Historical audit trail of all Hadoop MapReduce job executions with duration and records. |
| 13 | **User Management** | `/user-management.html` | Admin RBAC portal to manage users, toggle activation status, promote to Admin. |
| 14 | **Settings** | `/settings.html` | Dynamic heuristic threshold tuning, password change, infrastructure status. |
| 15 | **Logout** | `/login.html` | Secure session teardown and token invalidation. |

---

## 5. Indian Telecommunications CDR Data Standard

All data generated, processed, and searched conforms to the Indian National Numbering Plan (NNP):

- **Normalization**: Exactly 10 digits starting with **6, 7, 8, or 9** (supporting optional `+91` prefix).
- **Regions**: Real Indian cities and states (e.g., Bengaluru, Karnataka; Delhi NCR; Mumbai, Maharashtra; Hyderabad, Telangana; Chennai, Tamil Nadu; Kolkata, West Bengal).
- **CDR Attributes**:
  - `record_id`: Unique identifier (e.g. `CDR-10029`)
  - `caller_number`: 10-digit Indian mobile number
  - `receiver_number`: 10-digit Indian mobile number
  - `duration`: Call duration in seconds
  - `call_type`: `OUTGOING`, `INCOMING`, `INTERNATIONAL`, `MISSED`, `REJECTED`
  - `call_timestamp`: Timestamp format `yyyy-MM-dd HH:mm:ss`
  - `source_region` & `destination_region`: Indian Circle / State
  - `ip_address`: Originating SIP trunk / Gateway IP
  - `network_type`: `5G VoNR`, `4G VoLTE`, `4G LTE`, `3G/WCDMA`

---

## 6. Getting Started & Execution

### Demo Credentials:
- **Investigator**: `demo` / `demo123` (Role: `USER`)
- **Administrator**: `admin` / `admin123` (Role: `ADMIN`)

### Quick Launch (Windows):
Double-click `run.bat` or run:
```powershell
java -jar backend\target\telecom-backend-1.0.0.jar
```
Open **[http://localhost:8080](http://localhost:8080)** in any modern web browser.

### Rebuilding from Source:
```powershell
# 1. Package Hadoop MapReduce JAR
cd hadoop
..\mvnw.bat clean package
cd ..

# 2. Package Spring Boot Full-Stack JAR
.\mvnw.bat -f backend\pom.xml clean package "-Dmaven.test.skip=true"
```

---

## 7. Hadoop MapReduce CLI Execution (Cluster Mode)

To run the standalone Hadoop job on an Apache Hadoop 3.x cluster:

```bash
# Upload sample dataset to HDFS
hdfs dfs -mkdir -p /telecom/cdr/input
hdfs dfs -put datasets/sample-cdr.csv /telecom/cdr/input/

# Run MapReduce Fraud Detection Driver
hadoop jar hadoop/target/telecom-fraud-mapreduce-1.0.0.jar \
    com.telecom.fraud.mapreduce.FraudDetectionDriver \
    /telecom/cdr/input/sample-cdr.csv \
    /telecom/cdr/output/job_run_01

# Inspect output results
hdfs dfs -cat /telecom/cdr/output/job_run_01/part-r-00000 | head -n 25
```

---

## 8. Documentation Index

- [Architecture & Heuristic Model Specification](file:///c:/Users/Admin/OneDrive/Documents/Desktop/BDT/docs/architecture.md)
- [Setup & Installation Guide](file:///c:/Users/Admin/OneDrive/Documents/Desktop/BDT/docs/setup-guide.md)
- [Complete REST API Documentation](file:///c:/Users/Admin/OneDrive/Documents/Desktop/BDT/docs/api-documentation.md)

---

## 9. Academic Project Certification

- **Subject**: Big Data Technology (BDT)
- **Application**: TELECOM – Telecommunication Fraud Detection Using Hadoop MapReduce
- **Package**: `com.telecom`
- **Database**: `telecom`
- **Engine**: Apache Hadoop 3.3.6 MapReduce & Spring Boot 3.1.5
