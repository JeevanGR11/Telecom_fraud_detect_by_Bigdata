# TELECOM – REST API Documentation

All API endpoints are hosted at `/api/*` and return standard JSON payloads. Protected endpoints require a Bearer JWT token in the `Authorization` header:

```http
Authorization: Bearer <your_jwt_token>
```

---

## 1. Authentication Endpoints (`/api/auth`)

### 1.1 User Login
- **URL**: `POST /api/auth/login`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "username": "demo",
    "password": "demo123",
    "rememberMe": true
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Authentication successful",
    "token": "eyJhbGciOiJIUzI1NiIsIn...",
    "user": {
      "id": 2,
      "username": "demo",
      "email": "demo@telecom.in",
      "fullName": "Demo Investigator",
      "role": "USER",
      "status": "ACTIVE"
    }
  }
  ```

### 1.2 User Registration
- **URL**: `POST /api/auth/register`
- **Access**: Public
- **Request Body**:
  ```json
  {
    "fullName": "Priya Sharma",
    "username": "priya.s",
    "email": "priya.s@telecom.in",
    "password": "StrongPassword123!",
    "confirmPassword": "StrongPassword123!",
    "role": "USER"
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "success": true,
    "message": "Registration successful. You can now log in.",
    "token": null,
    "user": { ... }
  }
  ```

---

## 2. Dashboard Endpoints (`/api/dashboard`)

### 2.1 Summary KPIs
- **URL**: `GET /api/dashboard/stats`
- **Access**: Authenticated (`USER`, `ADMIN`)
- **Response** (`200 OK`):
  ```json
  {
    "totalCalls": 1000,
    "analyzedCalls": 1000,
    "fraudDetected": 3,
    "fraudDetectionRate": 0.3,
    "activeAlerts": 3,
    "totalSubscribers": 650,
    "monitoredIps": 12
  }
  ```

### 2.2 Dashboard Chart Series
- **URL**: `GET /api/dashboard/charts`
- **Access**: Authenticated (`USER`, `ADMIN`)
- **Response** (`200 OK`):
  Returns aggregated series for Call Volume Trends, Fraud Detection Timeline, Risk Level Distribution (High/Medium/Low), Call Type Breakdown, and Top Suspicious Mobile Numbers.

### 2.3 Recent Fraud Alerts Feed
- **URL**: `GET /api/dashboard/recent-alerts`
- **Access**: Authenticated (`USER`, `ADMIN`)
- **Response** (`200 OK`):
  List of top fraud alert objects with mobile number, total calls, distinct targets, risk score, risk level, and triggered reasons.

---

## 3. Fraud Investigation & Search (`/api/fraud`)

### 3.1 Investigate Indian Mobile Number
- **URL**: `GET /api/fraud/search/{number}`
- **Access**: Authenticated (`USER`, `ADMIN`)
- **Example**: `GET /api/fraud/search/8765432109`
- **Response** (`200 OK`):
  ```json
  {
    "found": true,
    "mobileNumber": "8765432109",
    "riskScore": 70,
    "riskClassification": "HIGH RISK",
    "totalCalls": 50,
    "incomingCalls": 12,
    "outgoingCalls": 38,
    "distinctReceivers": 28,
    "totalDurationSeconds": 642,
    "avgDurationSeconds": 21.4,
    "internationalCalls": 0,
    "nightCalls": 0,
    "triggeredRules": [
      "Abnormally high calling volume (50 calls)",
      "Excessive distinct receivers (28 targets)"
    ],
    "hourlyVolume": [0, 0, 0, 1, 4, 8, ...],
    "callHistory": [
      {
        "recordId": "CDR-10039",
        "callerNumber": "8765432109",
        "receiverNumber": "8195342410",
        "callDuration": 18,
        "callType": "OUTGOING",
        "callTimestamp": "2026-11-21 12:39:37",
        "sourceRegion": "Delhi, Delhi",
        "destinationRegion": "Ahmedabad, Gujarat",
        "ipAddress": "49.207.12.90",
        "networkType": "4G VoLTE"
      }
    ]
  }
  ```

### 3.2 List All Flagged Fraud Subscribers
- **URL**: `GET /api/fraud/results`
- **Access**: Authenticated (`USER`, `ADMIN`)
- **Response** (`200 OK`): List of all classified subscribers in the database.

---

## 4. Call Detail Records (CDR) (`/api/cdr`)

### 4.1 Query & Filter CDR Records
- **URL**: `GET /api/cdr/records` (or `GET /api/cdr`)
- **Parameters**:
  - `caller` (optional): Filter by caller phone number
  - `receiver` (optional): Filter by receiver phone number
  - `callType` (optional): `OUTGOING`, `INCOMING`, `INTERNATIONAL`, `MISSED`, `REJECTED`
  - `startDate`, `endDate` (optional): Format `yyyy-MM-dd`
  - `page` (default: 0), `size` (default: 15)
  - `sortBy` (default: `callTimestamp`), `direction` (`ASC` / `DESC`)
- **Response** (`200 OK`): Spring Data `Page<CallRecord>` with pagination metadata and items list.

### 4.2 Export CDR to CSV
- **URL**: `GET /api/cdr/export`
- **Access**: Authenticated
- **Response**: CSV stream attachment (`text/csv`).

---

## 5. Hadoop MapReduce Orchestration (`/api/hadoop`)

### 5.1 Check Hadoop & HDFS Status
- **URL**: `GET /api/hadoop/status`
- **Access**: Authenticated
- **Response** (`200 OK`):
  ```json
  {
    "hadoopAvailable": true,
    "clusterMode": "DISTRIBUTED / LOCAL_SIMULATION",
    "nameNodeUrl": "hdfs://localhost:9000",
    "inputHdfsPath": "/telecom/cdr/input",
    "outputHdfsPath": "/telecom/cdr/output",
    "activeJobs": 0,
    "lastJobStatus": "COMPLETED"
  }
  ```

### 5.2 Trigger MapReduce Fraud Detection Run
- **URL**: `POST /api/hadoop/start`
- **Access**: Admin only (`ROLE_ADMIN`)
- **Request Body**:
  ```json
  {
    "datasetName": "sample-cdr.csv",
    "frequencyThreshold": 8,
    "distinctReceiversThreshold": 6,
    "wangiriDurationThreshold": 5
  }
  ```
- **Response** (`200 OK`):
  ```json
  {
    "jobId": "JOB_HADOOP_20261124_001926",
    "status": "COMPLETED",
    "message": "MapReduce job executed and fraud scores updated successfully",
    "recordsProcessed": 1000,
    "suspiciousDetected": 3,
    "durationSeconds": 4
  }
  ```

### 5.3 Job Execution History
- **URL**: `GET /api/hadoop/jobs`
- **Access**: Authenticated (`USER`, `ADMIN`)

---

## 6. AI Clustering & Geo Analytics

### 6.1 AI Behavioral Clusters
- **URL**: `GET /api/clustering/results`
- **Response** (`200 OK`):
  Returns 4 behavioral profile clusters: Wangiri/Ping-Call Blast, High-Volume Robocall Spammers, Toll Bypass & IRSF, and Benign Standard Subscribers.

### 6.2 Geographic Circle Analytics
- **URL**: `GET /api/geo/analytics`
- **Response** (`200 OK`):
  Returns distribution across 14 Indian telecom circles, fraud rates per state, and regional hotspot coordinates.

---

## 7. Reports & Datasets

### 7.1 Generate Audit Report
- **URL**: `POST /api/reports/generate`
- **Access**: Admin only (`ROLE_ADMIN`)

### 7.2 Upload New CDR Dataset
- **URL**: `POST /api/datasets/upload`
- **Access**: Admin only (`ROLE_ADMIN`)
- **Request**: Multipart `file` (CSV format with schema validation).
