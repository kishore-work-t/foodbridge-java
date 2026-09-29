#!/usr/bin/env pwsh
<#
.SYNOPSIS
    FoodBridge Local Hosting Script - Runs backend + frontend locally
#>

param(
    [string]$MySqlPassword = "",
    [int]$BackendPort = 8080,
    [int]$FrontendPort = 5500
)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "    FoodBridge Local Hosting Launcher" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Yellow

$javaOk = (java -version 2>&1) -match "17|18|19|20|21"
$mvnOk = (mvn -version 2>&1) -match "Apache Maven"
$mysqlOk = (mysql --version 2>&1) -match "mysql"

if (-not $javaOk) { Write-Error "Java 17+ not found"; exit 1 }
if (-not $mvnOk) { Write-Error "Maven not found"; exit 1 }
if (-not $mysqlOk) { Write-Error "MySQL not found"; exit 1 }

Write-Host "✓ Java: OK" -ForegroundColor Green
Write-Host "✓ Maven: OK" -ForegroundColor Green
Write-Host "✓ MySQL: OK" -ForegroundColor Green

# Get MySQL password if not provided
if (-not $MySqlPassword) {
    $MySqlPassword = Read-Host "Enter MySQL root password" -AsSecureString
    $MySqlPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($MySqlPassword)
    )
}

# Set up database
Write-Host "`nSetting up database..." -ForegroundColor Yellow
$createDb = "CREATE DATABASE IF NOT EXISTS foodbridge_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p$MySqlPassword -e $createDb 2>$null
if ($LASTEXITCODE -ne 0) { Write-Error "Failed to create database. Check MySQL password."; exit 1 }
Write-Host "✓ Database ready" -ForegroundColor Green

# Update application.properties
Write-Host "`nConfiguring backend..." -ForegroundColor Yellow
$propsFile = "FoodBridge/backend/src/main/resources/application.properties"
$content = Get-Content $propsFile -Raw
$content = $content -replace 'spring\.datasource\.password=.*', "spring.datasource.password=$MySqlPassword"
$content = $content -replace 'server\.port=.*', "server.port=$BackendPort"
Set-Content $propsFile -Value $content
Write-Host "✓ Configuration updated" -ForegroundColor Green

# Start backend
Write-Host "`nStarting Spring Boot backend on port $BackendPort..." -ForegroundColor Yellow
$backendDir = "FoodBridge/backend"
$backendProcess = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendDir'; mvn spring-boot:run" -PassThru

# Wait for backend to start
Write-Host "Waiting for backend to start..." -ForegroundColor Yellow
$started = $false
for ($i = 0; $i -lt 60; $i++) {
    try {
        $response = Invoke-WebRequest -Uri "http://localhost:$BackendPort/api/auth/me" -Method Get -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 401) { $started = $true; break }
    } catch {}
    Start-Sleep 2
    Write-Host "." -NoNewline -ForegroundColor Gray
}
Write-Host ""

if (-not $started) {
    Write-Error "Backend failed to start within 120 seconds"
    Stop-Process $backendProcess.Id -Force
    exit 1
}
Write-Host "✓ Backend running at http://localhost:$BackendPort/api" -ForegroundColor Green

# Start frontend
Write-Host "`nStarting frontend on port $FrontendPort..." -ForegroundColor Yellow
$frontendDir = "FoodBridge/frontend"
$frontendProcess = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$frontendDir'; python -m http.server $FrontendPort" -PassThru

Start-Sleep 3
Write-Host "✓ Frontend running at http://localhost:$FrontendPort" -ForegroundColor Green

# Summary
Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "    FoodBridge is running!" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Frontend: http://localhost:$FrontendPort" -ForegroundColor Cyan
Write-Host "Backend API: http://localhost:$BackendPort/api" -ForegroundColor Cyan
Write-Host ""
Write-Host "Demo Credentials (password: password123):" -ForegroundColor Yellow
Write-Host "  Admin:       admin@foodbridge.com"
Write-Host "  Donor:       donor1@foodbridge.com"
Write-Host "  NGO (Verified): receiver1@foodbridge.com"
Write-Host ""
Write-Host "Press Ctrl+C to stop both servers" -ForegroundColor Gray

# Keep script running
try {
    while ($true) { Start-Sleep 10 }
} finally {
    Write-Host "`nStopping servers..." -ForegroundColor Yellow
    Stop-Process $backendProcess.Id -Force -ErrorAction SilentlyContinue
    Stop-Process $frontendProcess.Id -Force -ErrorAction SilentlyContinue
    Write-Host "Stopped." -ForegroundColor Green
}