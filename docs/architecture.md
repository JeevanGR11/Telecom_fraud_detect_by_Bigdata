# Architecture & System Design Specification

## Project: TELECOM – Telecommunication Fraud Detection Using Hadoop MapReduce

---

### 1. Executive System Overview

The **TELECOM** platform is an enterprise-grade Big Data Technology (BDT) telecommunications analytics system designed to process, analyze, and detect fraudulent calling patterns across large-scale Call Detail Record (CDR) datasets. Leveraging **Apache Hadoop MapReduce** for distributed batch processing, **Spring Boot 3** for backend business logic and REST services, and a modern **Light-Theme Responsive SaaS Dashboard**, the platform flags suspicious activities such as **Wangiri (Ping-Call) fraud**, **Robocall / Telemarketing spamming**, **International Revenue Share Fraud (IRSF) / Toll Bypass**, and **SIM Box burst dialing**.

```
+-----------------------------------------------------------------------------------+
|                                 TELECOM PLATFORM                                  |
+-----------------------------------------------------------------------------------+
|                                                                                   |
|  +--------------------+     +---------------------+     +----------------------+  |
|  | Modern Web UI      |     | Spring Boot 3 Core  |     | Distributed Engine   |  |
|  | (Light Theme SaaS) | <-> | (Java 17/26, Spring | <-> | (Apache Hadoop 3.3.6 |  |
|  | - Bootstrap 5      |     |  Security, JPA)     |     |  HDFS & MapReduce)   |  |
|  | - Chart.js 4       |     |                     |     |                      |  |
|  +--------------------+     +----------+----------+     +----------------------+  |
|                                        |                                          |
|                             +----------+----------+                               |
|                             | Relational Store    |                               |
|                             | (MySQL 8 / H2 File) |                               |
|                             | - telecom Database  |                               |
|                             +---------------------+                               |
+-----------------------------------------------------------------------------------+
```

---

### 2. Architectural Layers

The system follows a strict layered architectural model:

#### 2.1 Presentation Layer (Frontend)
- **Technology**: HTML5, Vanilla JavaScript (ES6+), Bootstrap 5.3, Bootstrap Icons, Chart.js 4.4.
- **Theme Standard**: Strict **Light Theme** (`#FFFFFF` background, `#F8FAFC` secondary, `#E63946` brand red accent, `#1E293B` typography, `#E2E8F0` borders).
- **Core Modules**:
  1. `login.html` & `register.html`: Secure authentication, show/hide password, client validation, demo autofill.
  2. `dashboard.html`: 4 core KPI cards, 5 interactive charts (Call Volume, Fraud Trends, Risk Distribution, Call Types, Top Suspicious Numbers), live alert feed, and quick actions.
  3. `search-fraud.html`: Single-subscriber Indian mobile number investigator (10-digit validation, risk score gauge, call history table, hourly distribution).
  4. `call-records.html`: Full CDR explorer with pagination, multi-attribute filtering (caller, receiver, call type, date range), and CSV export.
  5. `fraud-detection.html`: MapReduce job orchestration console, detection threshold sliders, execution pipeline diagram.
  6. `ip-threats.html`: Network IP gateway monitoring, SIM box threat categorization.
  7. `geo-analysis.html`: Telecom circle and Indian state geographic density mapping and risk analysis.
  8. `ai-clustering.html`: K-Means behavioral clustering (Wangiri, Robocallers, Toll Bypass, Benign).
  9. `reports.html`: Audit report generator with printable view and CSV downloads.
  10. `datasets.html`: Synthetic CDR generator (1K, 10K, 100K) and CSV dataset uploader with schema validator.
  11. `analysis-history.html`: Job execution audit log showing start/end timestamps and records processed.
  12. `user-management.html`: Role-Based Access Control (RBAC) user administration.
  13. `settings.html`: Heuristic threshold configuration and password manager.

#### 2.2 Security & Gateway Layer
- **Token Security**: Stateless JSON Web Token (JWT) using HMAC-SHA256 (`JwtUtil`).
- **Authorization**: Spring Security filter chain enforcing Role-Based Access Control (`ROLE_ADMIN`, `ROLE_USER`).
- **Password Protection**: BCrypt one-way password hashing (work factor 10). Passwords are never stored in plain text.
- **CORS & CSRF**: Granular CORS origin configuration and CSRF protection tuned for stateless API clients.

#### 2.3 Backend Business & Service Layer (`com.telecom.service`)
- `HadoopService`: Interacts with Hadoop HDFS and coordinates MapReduce job submission, tracking, and output ingestion.
- `FraudDetectionService`: Executes real-time subscriber heuristic scoring, rules engine evaluation, and timeline metrics.
- `CDRService`: Manages dataset ingestion, CSV streaming, phone number normalization, and paginated searches.
- `AIClusteringService`: Implements multi-dimensional behavioral grouping to cluster subscribers based on duration, frequency, receiver entropy, and time patterns.
- `GeoAnalysisService`: Maps telecom metrics across 14 primary Indian telecommunications circles.
- `IPThreatService`: Correlates SIP trunk and VoIP IP addresses with suspicious calling bursts.
- `DashboardService`: Aggregates real-time KPI metrics and Chart.js series directly from repository data.
- `ReportService`: Synthesizes investigative audit reports.

#### 2.4 Distributed Big Data Processing Layer (`hadoop/`)
- **Engine**: Apache Hadoop 3.3.6 MapReduce.
- **Custom Hadoop Driver**: `FraudDetectionDriver.java`.
- **Hadoop Mapper**: `CDRFraudMapper.java` extracts `caller_number` as the key and emits serialized `CDRRecordWritable` instances containing duration, call type, timestamp, receiver number, and network flags.
- **Hadoop Reducer**: `CDRFraudReducer.java` computes distributed aggregations per mobile subscriber:
  - Total call volume (\(N_{calls}\))
  - Distinct receivers set cardinality (\(N_{receivers}\))
  - Cumulative talk time (\(T_{total}\)) and average call duration (\(\bar{T}\))
  - Night call ratio (\(R_{night} = \frac{N_{00-05}}{N_{calls}}\))
  - International call frequency (\(N_{ISD}\))
  - Computes composite fraud risk score (\(S_{risk} \in [0, 100]\)) and emits tab-separated analytical records.

#### 2.5 Persistence & Database Layer
- **Target Database**: MySQL 8 (`telecom` database) with seamless persistent H2 fallback (`data/telecomdb.mv.db`).
- **ORM / Data Access**: Spring Data JPA / Hibernate 6.
- **Tables**: `users`, `datasets`, `call_records`, `fraud_results`, `threat_records`, `analysis_jobs`, `reports`, `audit_logs`, `system_settings`.

---

### 3. Fraud Detection Mathematical & Heuristic Scoring Model

The risk score \(S_{risk}\) is evaluated on a bounded integer scale \(0 \le S_{risk} \le 100\):

$$S_{risk} = \min\left(100, \; w_1 \cdot F_{vol} + w_2 \cdot F_{rec} + w_3 \cdot F_{wangiri} + w_4 \cdot F_{dur} + w_5 \cdot F_{night} + w_6 \cdot F_{isd}\right)$$

Where the behavioral feature flags and weights are:

| Fraud Vector | Condition / Criterion | Score Weight |
|---|---|---|
| **Wangiri / Ping-Call** | \(N_{calls} \ge 5 \land \bar{T} \le 5\text{s} \land \frac{N_{receivers}}{N_{calls}} \ge 0.7\) | **+35 pts** |
| **Robocall / Spammer** | \(N_{calls} \ge 20 \land N_{receivers} \ge 15\) | **+30 pts** |
| **High Frequency Blast** | \(N_{calls} \ge 10\) | **+15 pts** |
| **High Target Diversity** | \(N_{receivers} \ge 8\) | **+15 pts** |
| **Night Activity Anomaly** | \(R_{night} \ge 0.35 \land N_{calls} \ge 4\) | **+15 pts** |
| **Toll Bypass / Long Calls** | \(T_{total} \ge 3600\text{s} \lor \bar{T} \ge 900\text{s}\) | **+20 pts** |
| **International Spiking** | \(N_{ISD} \ge 5\) | **+15 pts** |

#### Risk Classifications:
- **0 – 39**: **LOW RISK** (Standard legitimate subscriber usage)
- **40 – 69**: **MEDIUM RISK** (Elevated outbound dialing, potential commercial marketing)
- **70 – 100**: **HIGH RISK** (Suspected Wangiri, SIM Box blast, or PBX toll bypass)

---

### 4. Indian Mobile Number Normalization & Validation Standard

All numbers entering the system are strictly normalized and validated:
1. Strips all non-digit characters (`+`, spaces, hyphens, parentheses).
2. Trims leading country code `91` (if 12 digits) or trunk prefix `0` (if 11 digits).
3. Validates against canonical Indian Department of Telecommunications (DoT) regex:
   ```regex
   ^[6-9]\d{9}$
   ```
4. Exactly 10 digits starting with **6, 7, 8, or 9**.
