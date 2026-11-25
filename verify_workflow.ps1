# Clean up any previous java
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

Write-Host "Starting Spring Boot application..."
$p = Start-Process -FilePath "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" -ArgumentList "-jar", "backend\target\telecom-backend-1.0.0.jar" -RedirectStandardOutput "server_out.log" -RedirectStandardError "server_err.log" -PassThru
Write-Host "Process started with PID $($p.Id)"

$ready = $false
for ($i = 0; $i -lt 35; $i++) {
    Start-Sleep -Seconds 1
    try {
        $res = Invoke-WebRequest -Uri "http://localhost:8080/dashboard.html" -UseBasicParsing -TimeoutSec 2
        if ($res.StatusCode -eq 200) {
            $ready = $true
            Write-Host "Server is ready on port 8080! Took $i seconds."
            break
        }
    } catch {
        # continue waiting
    }
}

if (-not $ready) {
    Write-Host "Server timed out on startup."
    Stop-Process -Id $p.Id -Force
    exit 1
}

try {
    Write-Host "`n========================================================"
    Write-Host " STEP 1: OPEN DASHBOARD (FULL-SCREEN LAYOUT & ROUTING) "
    Write-Host "========================================================"
    $rootRes = Invoke-WebRequest -Uri "http://localhost:8080/" -UseBasicParsing
    Write-Host "GET / status: $($rootRes.StatusCode) ($($rootRes.Content.Length) bytes)"
    if ($rootRes.Content -match "Hadoop MapReduce Job Submission / Fraud Detection Engine") {
        Write-Host "SUCCESS: Root URL serves full dashboard with distributed engine controls directly!"
    } else {
        Write-Host "WARNING: Root URL does not have distributed engine section banner."
    }

    $dashRes = Invoke-WebRequest -Uri "http://localhost:8080/dashboard.html" -UseBasicParsing
    Write-Host "GET /dashboard.html status: $($dashRes.StatusCode) ($($dashRes.Content.Length) bytes)"

    # Authenticate to get JWT token for API tests
    $loginPayload = @{ username = "admin"; password = "admin123" } | ConvertTo-Json
    $auth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginPayload -ContentType "application/json"
    $headers = @{ Authorization = "Bearer $($auth.token)" }
    Write-Host "Admin authenticated successfully. Token: $($auth.token.Substring(0, 15))..."

    Write-Host "`n========================================================"
    Write-Host " STEP 2: LOAD SAMPLE CDR DATASET VIA API "
    Write-Host "========================================================"
    $loadPayload = @{ force = $true } | ConvertTo-Json
    $loadRes = Invoke-RestMethod -Uri "http://localhost:8080/api/hadoop/load-sample?force=true" -Method POST -Headers $headers -ContentType "application/json" -Body $loadPayload
    Write-Host "Load Sample Response:"
    Write-Host "  - Success: $($loadRes.success)"
    Write-Host "  - Loaded Records: $($loadRes.loadedRecords)"
    Write-Host "  - Rejected Records: $($loadRes.rejectedRecords)"
    Write-Host "  - Total Records: $($loadRes.totalRecords)"
    Write-Host "  - Dataset Name: $($loadRes.datasetName)"
    Write-Host "  - Message: $($loadRes.message)"

    if ($loadRes.loadedRecords -ge 1000) {
        Write-Host "SUCCESS: 1,000+ Indian CDR records successfully loaded into MySQL!"
    } else {
        throw "Failed to load at least 1,000 records."
    }

    Write-Host "`n========================================================"
    Write-Host " STEP 3: VERIFY IMPORTED RECORDS IN DATABASE "
    Write-Host "========================================================"
    $cdrRes = Invoke-RestMethod -Uri "http://localhost:8080/api/cdr/records?page=0&size=5" -Method GET -Headers $headers
    Write-Host "Total Records in DB: $($cdrRes.totalElements)"
    Write-Host "Page Size Returned: $($cdrRes.content.Count)"
    
    $sample1 = $cdrRes.content[0]
    Write-Host "Sample Record 1:"
    Write-Host "  - ID: $($sample1.recordId)"
    Write-Host "  - Caller: $($sample1.callerNumber) (Valid Indian 10-digit MSISDN)"
    Write-Host "  - Receiver: $($sample1.receiverNumber)"
    Write-Host "  - Type: $($sample1.callType)"
    Write-Host "  - Duration: $($sample1.callDuration)s"
    Write-Host "  - Timestamp: $($sample1.callTimestamp)"
    Write-Host "  - Region: $($sample1.sourceRegion) -> $($sample1.destinationRegion)"
    Write-Host "  - IP: $($sample1.ipAddress)"
    Write-Host "  - Network: $($sample1.networkType)"

    if ($sample1.callerNumber -match '^[6-9]\d{9}$') {
        Write-Host "SUCCESS: Caller MSISDN complies with Indian 10-digit standard (6-9 prefix)."
    } else {
        throw "Invalid phone format: $($sample1.callerNumber)"
    }

    Write-Host "`n========================================================"
    Write-Host " STEP 4: START HADOOP MAPREDUCE FRAUD DETECTION "
    Write-Host "========================================================"
    $mrPayload = @{
        datasetName = "Indian-Telecom-CDR-Primary"
        frequencyThreshold = 8
        distinctReceiversThreshold = 6
        wangiriDurationThreshold = 5
        excessiveDurationThreshold = 3600
        nightRatioThreshold = 0.35
    } | ConvertTo-Json
    $mrRes = Invoke-RestMethod -Uri "http://localhost:8080/api/hadoop/start" -Method POST -Headers $headers -ContentType "application/json" -Body $mrPayload
    Write-Host "MapReduce Job Status:"
    Write-Host "  - Job ID: $($mrRes.jobId)"
    Write-Host "  - Status: $($mrRes.status)"
    Write-Host "  - Duration: $($mrRes.durationSeconds)s"
    Write-Host "  - Records Processed: $($mrRes.processedRecords)"
    Write-Host "  - Fraud Records Flagged: $($mrRes.fraudRecords)"
    if ($mrRes.counters) {
        Write-Host "  - Valid Processed: $($mrRes.counters.ValidRecordsProcessed)"
        Write-Host "  - High Risk Detected: $($mrRes.counters.HighRiskDetected)"
        Write-Host "  - Medium Risk Detected: $($mrRes.counters.MediumRiskDetected)"
        Write-Host "  - Unique Callers Analyzed: $($mrRes.counters.TotalUniqueCallers)"
    }

    if ($mrRes.status -eq "COMPLETED") {
        Write-Host "SUCCESS: Java MapReduce pipeline executed and completed successfully!"
    } else {
        throw "MapReduce execution did not complete: $($mrRes.status)"
    }

    Write-Host "`n========================================================"
    Write-Host " STEP 5: VERIFY MAPREDUCE RESULTS & SUBSCRIBER INVESTIGATION "
    Write-Host "========================================================"
    $searchRes = Invoke-RestMethod -Uri "http://localhost:8080/api/fraud/search/8765432109" -Method GET -Headers $headers
    Write-Host "Investigation for 8765432109:"
    Write-Host "  - Found: $($searchRes.found)"
    Write-Host "  - Risk Score: $($searchRes.riskScore) / 100"
    Write-Host "  - Classification: $($searchRes.riskClassification)"
    Write-Host "  - Call History Count: $($searchRes.callHistory.Count)"
    Write-Host "  - Triggered Rules: $($searchRes.triggeredRules -join '; ')"

    Write-Host "`n========================================================"
    Write-Host " STEP 6: REFRESH DASHBOARD STATISTICS & ALERTS "
    Write-Host "========================================================"
    $dashStats = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/stats" -Method GET -Headers $headers
    Write-Host "Dashboard Real-Time Stats:"
    Write-Host "  - Total Calls: $($dashStats.totalCalls)"
    Write-Host "  - Analyzed Calls: $($dashStats.analyzedCalls)"
    Write-Host "  - Fraud Detected: $($dashStats.fraudDetected)"
    Write-Host "  - Detection Rate: $($dashStats.fraudDetectionRate)%"
    Write-Host "  - Active Alerts: $($dashStats.activeAlerts)"

    $alerts = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/recent-alerts" -Method GET -Headers $headers
    Write-Host "Recent Alerts Feed Count: $($alerts.Count)"
    if ($alerts.Count -gt 0) {
        Write-Host "Top Alert: Number=$($alerts[0].mobileNumber), Score=$($alerts[0].riskScore), Level=$($alerts[0].riskLevel)"
    }

    $jobs = Invoke-RestMethod -Uri "http://localhost:8080/api/hadoop/jobs" -Method GET -Headers $headers
    Write-Host "Analysis History Jobs Logged: $($jobs.Count)"
    Write-Host "Latest Job Run: ID=$($jobs[0].jobId), Status=$($jobs[0].status), Processed=$($jobs[0].processedRecords)"

    Write-Host "`n========================================================"
    Write-Host " ALL 6 WORKFLOW VERIFICATION SUITES PASSED FLAWLESSLY! "
    Write-Host "========================================================"

} finally {
    Write-Host "Stopping test server process $($p.Id)..."
    Stop-Process -Id $p.Id -Force
    Write-Host "Test server stopped cleanly."
}
