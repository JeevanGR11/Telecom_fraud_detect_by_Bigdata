@echo off
title TELECOM - Fraud Detection System Using Hadoop MapReduce
echo ===============================================================================
echo       TELECOM - Telecommunication Fraud Detection Using Hadoop MapReduce
echo                     Big Data Technology (BDT) Project
echo ===============================================================================
echo.
echo Starting Spring Boot Backend and Web UI Server on port 8080...
echo.
echo Demo Credentials:
echo   - Investigator: demo  / demo123  (Role: USER)
echo   - Administrator: admin / admin123 (Role: ADMIN)
echo.
echo Access URL: http://localhost:8080
echo.
echo Press Ctrl+C in this terminal window to stop the server.
echo ===============================================================================
echo.

if exist "backend\target\telecom-backend-1.0.0.jar" (
    java -jar "backend\target\telecom-backend-1.0.0.jar"
) else (
    echo [ERROR] backend\target\telecom-backend-1.0.0.jar was not found!
    echo Building project now...
    call mvnw.bat -f backend\pom.xml package -Dmaven.test.skip=true
    if exist "backend\target\telecom-backend-1.0.0.jar" (
        java -jar "backend\target\telecom-backend-1.0.0.jar"
    ) else (
        echo [ERROR] Build failed. Please check your Java installation.
        pause
    )
)
pause
