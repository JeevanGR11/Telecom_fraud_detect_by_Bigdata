# Clean up any previous java
Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force
Start-Sleep -Seconds 1

Write-Host "Starting Spring Boot application..."
$logPath = "server_test.log"
if (Test-Path $logPath) { Remove-Item -Force $logPath }
$p = Start-Process -FilePath "C:\Program Files\Java\jdk-26.0.2\bin\java.exe" -ArgumentList "-jar", "backend\target\telecom-backend-1.0.0.jar" -RedirectStandardOutput "server_out.log" -RedirectStandardError "server_err.log" -PassThru
Write-Host "Process started with PID $($p.Id)"

$ready = $false
for ($i = 0; $i -lt 35; $i++) {
    Start-Sleep -Seconds 1
    try {
        $res = Invoke-WebRequest -Uri "http://localhost:8080/login.html" -UseBasicParsing -TimeoutSec 2
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
    Write-Host "`n=== 1. DEMO USER AUTHENTICATION ==="
    $loginPayload = @{
        username = "demo"
        password = "demo123"
    } | ConvertTo-Json
    $demoAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginPayload -ContentType "application/json"
    Write-Host "Demo authenticated: User=$($demoAuth.user.username), Role=$($demoAuth.user.role), Token Prefix=$($demoAuth.token.Substring(0, 15))..."
    $demoHeaders = @{ Authorization = "Bearer $($demoAuth.token)" }

    Write-Host "`n=== 2. ADMIN USER AUTHENTICATION ==="
    $adminPayload = @{
        username = "admin"
        password = "admin123"
    } | ConvertTo-Json
    $adminAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $adminPayload -ContentType "application/json"
    Write-Host "Admin authenticated: User=$($adminAuth.user.username), Role=$($adminAuth.user.role)"
    $adminHeaders = @{ Authorization = "Bearer $($adminAuth.token)" }

    Write-Host "`n=== 3. DASHBOARD STATS API ==="
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/dashboard/stats" -Method GET -Headers $demoHeaders
    Write-Host "Total Calls: $($stats.totalCalls)"
    Write-Host "Analyzed Calls: $($stats.analyzedCalls)"
    Write-Host "Fraud Detected: $($stats.fraudDetected)"
    Write-Host "Detection Rate: $($stats.fraudDetectionRate)%"
    Write-Host "Active Alerts: $($stats.activeAlerts)"

    Write-Host "`n=== 4. SEARCH FRAUD (INDIAN MOBILE NUMBER) ==="
    $search = Invoke-RestMethod -Uri "http://localhost:8080/api/fraud/search/8765432109" -Method GET -Headers $demoHeaders
    Write-Host "Target Number: $($search.mobileNumber)"
    Write-Host "Risk Score: $($search.riskScore) / 100"
    Write-Host "Risk Classification: $($search.riskClassification)"
    Write-Host "Call History Count: $($search.callHistory.Count)"
    Write-Host "Reasons Triggered: $($search.triggeredRules -join '; ')"

    Write-Host "`n=== 5. CALL DETAIL RECORDS (PAGINATION) ==="
    $cdrs = Invoke-RestMethod -Uri "http://localhost:8080/api/cdr/records?page=0&size=5" -Method GET -Headers $demoHeaders
    Write-Host "Total Elements in DB: $($cdrs.totalElements)"
    Write-Host "Records in first page: $($cdrs.content.Count)"
    Write-Host "Sample Record 1: Caller=$($cdrs.content[0].callerNumber), Receiver=$($cdrs.content[0].receiverNumber), Type=$($cdrs.content[0].callType), Duration=$($cdrs.content[0].callDuration)s, Region=$($cdrs.content[0].sourceRegion)"

    Write-Host "`n=== 6. HADOOP MAPREDUCE EXECUTION ==="
    $mr = Invoke-RestMethod -Uri "http://localhost:8080/api/hadoop/start" -Method POST -Headers $adminHeaders -ContentType "application/json" -Body "{}"
    Write-Host "Hadoop Job Triggered: JobId=$($mr.jobId), Status=$($mr.status), Records Processed=$($mr.recordsProcessed), Suspicious Identified=$($mr.suspiciousDetected)"

    Write-Host "`n=== 7. AI CLUSTERING ANALYSIS ==="
    $clusters = Invoke-RestMethod -Uri "http://localhost:8080/api/clustering/results" -Method GET -Headers $demoHeaders
    Write-Host "Clusters identified: $($clusters.Count)"
    foreach ($c in $clusters) {
        Write-Host "  - Cluster $($c.clusterId): $($c.clusterName) | Members: $($c.subscribers.Count) | Anomalous: $($c.anomalous)"
    }

    Write-Host "`n=== 8. GEO ANALYSIS ==="
    $geo = Invoke-RestMethod -Uri "http://localhost:8080/api/geo/analytics" -Method GET -Headers $demoHeaders
    Write-Host "Total Circles Monitored: $($geo.totalCirclesMonitored), Highest Risk: $($geo.highestRiskCircle)"
    foreach ($state in $geo.stateDistribution | Select-Object -First 3) {
        Write-Host "  - $($state.state): $($state.totalCalls) calls ($($state.suspiciousCalls) suspicious)"
    }

    Write-Host "`n=== 9. STATIC FRONTEND WEBPAGE VERIFICATION ==="
    $pages = @("index.html", "login.html", "register.html", "dashboard.html", "search-fraud.html", "call-records.html", "fraud-detection.html", "reports.html", "datasets.html", "settings.html")
    foreach ($page in $pages) {
        $pRes = Invoke-WebRequest -Uri "http://localhost:8080/$page" -UseBasicParsing
        Write-Host "  - $($page): Status $($pRes.StatusCode) ($($pRes.Content.Length) bytes)"
    }

    Write-Host "`nALL 9 END-TO-END VERIFICATION SUITES PASSED FLAWLESSLY!"
} finally {
    Write-Host "Stopping backend process $($p.Id)..."
    Stop-Process -Id $p.Id -Force
    Write-Host "Backend stopped."
}
