 it# Resource Manager Pro - Quick Start Script
# This script will start the emulator, install, and launch the app

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Resource Manager Pro - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if an emulator is running
Write-Host "[1/4] Checking for connected devices..." -ForegroundColor Yellow
$devices = adb devices | Select-String -Pattern "device$"

if ($devices.Count -eq 0) {
    Write-Host "No device found. Please start an emulator first:" -ForegroundColor Red
    Write-Host "  - Option A: Open Android Studio > Tools > Device Manager > Start Emulator" -ForegroundColor White
    Write-Host "  - Option B: Run: emulator -avd Pixel_8" -ForegroundColor White
    Write-Host ""
    Write-Host "After starting emulator, run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "✓ Device connected!" -ForegroundColor Green
Write-Host ""

# Step 2: Set Java Home
Write-Host "[2/4] Setting up Java environment..." -ForegroundColor Yellow
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
Write-Host "✓ Java Home configured" -ForegroundColor Green
Write-Host ""

# Step 3: Build and Install
Write-Host "[3/4] Building and installing app (this may take a minute)..." -ForegroundColor Yellow
$result = .\gradlew.bat :app:installDebug --no-daemon 2>&1

if ($LASTEXITCODE -ne 0) {
    Write-Host "✗ Installation failed!" -ForegroundColor Red
    Write-Host "Try running manually:" -ForegroundColor Yellow
    Write-Host "  .\gradlew.bat :app:installDebug" -ForegroundColor White
    exit 1
}

Write-Host "✓ App installed successfully!" -ForegroundColor Green
Write-Host ""

# Step 4: Launch App
Write-Host "[4/4] Launching app..." -ForegroundColor Yellow
adb shell am start -n com.cuea.rmp.mobile/.MainActivity

Write-Host "✓ App launched!" -ForegroundColor Green
Write-Host ""

# Display login credentials
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  🔐 Test Login Credentials" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Email:    " -NoNewline; Write-Host "tester@rmp.local" -ForegroundColor Green
Write-Host "Password: " -NoNewline; Write-Host "test123" -ForegroundColor Green
Write-Host "(or any non-empty password)" -ForegroundColor DarkGray
Write-Host ""

Write-Host "✅ App is ready! Check your emulator." -ForegroundColor Green
Write-Host ""
Write-Host "To view logs: adb logcat --pid=`$(adb shell pidof -s com.cuea.rmp.mobile)" -ForegroundColor DarkGray

