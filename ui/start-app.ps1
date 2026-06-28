# Resource Manager Pro - Quick Start Script
# This script will build, install, and launch the app on the emulator

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Resource Manager Pro - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"

# Step 1: Check if an emulator is running
Write-Host "[1/4] Checking for connected devices..." -ForegroundColor Yellow
$devices = & $ADB devices 2>$null | Select-String -Pattern "device$"

if ($devices.Count -eq 0) {
    Write-Host "No device found. Please start an emulator first:" -ForegroundColor Red
    Write-Host "  Option A: Open Android Studio > Tools > Device Manager > Start Emulator" -ForegroundColor White
    Write-Host "  Option B: Run run-app.ps1 in the root folder (auto-starts emulator)" -ForegroundColor White
    Write-Host ""
    Write-Host "After starting emulator, run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "  OK Device connected!" -ForegroundColor Green
Write-Host ""

# Step 2: Set Java Home
Write-Host "[2/4] Setting up Java environment..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Write-Host "  OK Java Home configured" -ForegroundColor Green
Write-Host ""

# Step 3: Build and Install
Write-Host "[3/4] Building and installing app (this may take a minute)..." -ForegroundColor Yellow
.\gradlew.bat ":app:installDebug" "--daemon"

if ($LASTEXITCODE -ne 0) {
    Write-Host "  FAILED Installation failed!" -ForegroundColor Red
    Write-Host "  Try running manually: .\gradlew.bat :app:installDebug" -ForegroundColor Yellow
    exit 1
}

Write-Host "  OK App installed successfully!" -ForegroundColor Green
Write-Host ""

# Step 4: Launch App
Write-Host "[4/4] Launching app..." -ForegroundColor Yellow
& $ADB shell am start -n com.cuea.rmp.mobile/.MainActivity

Write-Host "  OK App launched!" -ForegroundColor Green
Write-Host ""

# Display login credentials
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Test Login Credentials" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Email:    " -NoNewline; Write-Host "tester@rmp.local" -ForegroundColor Green
Write-Host "  Password: " -NoNewline; Write-Host "test123" -ForegroundColor Green
Write-Host ""
Write-Host "  App is ready! Check your emulator." -ForegroundColor Green
Write-Host ""
