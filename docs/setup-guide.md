# TELECOM – Setup & Installation Guide

This guide provides complete instructions to set up, configure, and execute the **TELECOM Telecommunication Fraud Detection Platform** on Windows.

---

## 1. Prerequisites

Before running the application, ensure the following software is installed on your system:

| Component | Minimum Version | Verified Version | Purpose |
|---|---|---|---|
| **Java Development Kit (JDK)** | Java 17 | JDK 26.0.2 / 17 LTS | Backend execution & MapReduce |
| **Apache Maven** | 3.8+ | 3.9.16 (bundled wrapper `mvnw.bat`) | Dependency management & packaging |
| **MySQL Database (Optional)** | 8.0+ | MySQL 8.0 / 8.3 | Production persistence store |
| **Apache Hadoop (Optional)** | 3.2+ | Hadoop 3.3.6 | Distributed cluster processing |
| **Web Browser** | Modern | Chrome / Edge / Firefox | Modern Light-Theme Dashboard |

> **Note**: The application includes an automatic **zero-configuration embedded persistence fallback** (`data/telecomdb.mv.db`), so you can launch and run the entire full-stack application immediately without requiring a pre-installed MySQL or Hadoop service!

---

## 2. Fast-Start Execution (Recommended)

1. Open PowerShell or Command Prompt in the project root directory:
   ```powershell
   cd c:\Users\Admin\OneDrive\Documents\Desktop\BDT
   ```

2. Run the application using the pre-compiled JAR:
   ```powershell
   java -jar backend\target\telecom-backend-1.0.0.jar
   ```
   *(Or double-click `run.bat`)*

3. Once you see the startup banner:
   ```text
   Tomcat started on port(s): 8080 (http) with context path ''
   Started TelecomApplication in ... seconds
   ```

4. Open your web browser and navigate to:
   ```
   http://localhost:8080
   ```

---

## 3. Demo Credentials

The platform is pre-loaded with secure accounts initialized via BCrypt password hashing:

| Role | Username | Password | Access Privileges |
|---|---|---|---|
| **Investigator (User)** | `demo` | `demo123` | Dashboard, Search Fraud, CDR View, AI Clustering, Geo Analysis |
| **Administrator** | `admin` | `admin123` | Full access + Trigger MapReduce, Upload Datasets, RBAC, Settings |

---

## 4. Building from Source

To compile and package both the Hadoop MapReduce engine and Spring Boot backend from source:

### 4.1 Build Hadoop MapReduce Module
```powershell
cd hadoop
..\mvnw.bat clean package
cd ..
```
*Output artifact:* `hadoop/target/telecom-fraud-mapreduce-1.0.0.jar`

### 4.2 Build Spring Boot Backend & Frontend Bundle
```powershell
.\mvnw.bat -f backend\pom.xml clean package "-Dmaven.test.skip=true"
```
*Output artifact:* `backend/target/telecom-backend-1.0.0.jar`

---

## 5. Configuring MySQL 8 Database (Optional Production Mode)

To run the application against a live MySQL 8 database instead of the embedded engine:

1. Open MySQL CLI or MySQL Workbench and create the `telecom` database:
   ```sql
   CREATE DATABASE telecom CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Execute the schema and seed scripts:
   ```powershell
   mysql -u root -p telecom < database\schema.sql
   mysql -u root -p telecom < database\sample-data.sql
   ```

3. Update `backend/src/main/resources/application.properties` (or run with `--spring.profiles.active=mysql`):
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/telecom?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
   ```

4. Launch the application:
   ```powershell
   java -jar backend\target\telecom-backend-1.0.0.jar --spring.profiles.active=mysql
   ```

---

## 6. Configuring Hadoop HDFS Cluster (Optional Distributed Cluster Mode)

When connected to an active Apache Hadoop 3.x cluster:

1. Create input and output directories in HDFS:
   ```bash
   hdfs dfs -mkdir -p /telecom/cdr/input
   hdfs dfs -mkdir -p /telecom/cdr/output
   ```

2. Upload a CDR dataset to HDFS:
   ```bash
   hdfs dfs -put datasets/sample-cdr.csv /telecom/cdr/input/
   ```

3. Execute the Hadoop MapReduce job from the CLI:
   ```bash
   hadoop jar hadoop/target/telecom-fraud-mapreduce-1.0.0.jar \
       com.telecom.fraud.mapreduce.FraudDetectionDriver \
       /telecom/cdr/input/sample-cdr.csv \
       /telecom/cdr/output/job_run_01
   ```

4. Inspect the resulting fraud metrics:
   ```bash
   hdfs dfs -cat /telecom/cdr/output/job_run_01/part-r-00000 | head -n 20
   ```

5. In the web dashboard (`http://localhost:8080/settings.html`), configure the NameNode URL (`hdfs://localhost:9000`) to let the web UI trigger and track cluster jobs automatically.
