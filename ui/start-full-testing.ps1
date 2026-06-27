# Resource Manager Pro - Start Backend & Frontend Together
# This script helps you test the full integration

Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║  Resource Manager Pro - Integration Testing Startup  ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check prerequisites
Write-Host "[1/6] Checking prerequisites..." -ForegroundColor Yellow

# Check if backend directory exists
$backendPath = "C:\Users\Munji\projects\RessourceMnagerPro\api\ResourceManagerPro"
if (-not (Test-Path $backendPath)) {
    Write-Host "✗ Backend directory not found at: $backendPath" -ForegroundColor Red
    exit 1
}

Write-Host "✓ Backend directory found" -ForegroundColor Green

# Check if emulator is running
$devices = adb devices | Select-String -Pattern "device$"
if ($devices.Count -eq 0) {
    Write-Host "⚠ No emulator detected. Please start one first:" -ForegroundColor Yellow
    Write-Host "  - Open Android Studio > Tools > Device Manager > Start Emulator" -ForegroundColor White
    Write-Host ""
    $response = Read-Host "Start the backend anyway? (Y/N)"
    if ($response -ne "Y" -and $response -ne "y") {
        exit 0
    }
} else {
    Write-Host "✓ Emulator connected" -ForegroundColor Green
}

Write-Host ""

# Start Backend
Write-Host "[2/6] Starting Spring Boot backend..." -ForegroundColor Yellow
Write-Host "(This will open in a new window)" -ForegroundColor DarkGray

$backendJob = Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$backendPath'; Write-Host 'Starting Resource Manager Pro Backend...' -ForegroundColor Cyan; .\mvnw.cmd spring-boot:run" -PassThru

Write-Host "✓ Backend starting... (PID: $($backendJob.Id))" -ForegroundColor Green
Write-Host "  Waiting for backend to be ready (this takes ~20-30 seconds)..." -ForegroundColor DarkGray

# Wait for backend to start
$maxWait = 60
$waited = 0
$backendReady = $false

while ($waited -lt $maxWait -and -not $backendReady) {
    Start-Sleep -Seconds 2
    $waited += 2

    try {
        $response = Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -TimeoutSec 1 -ErrorAction SilentlyContinue
        if ($response.StatusCode -eq 200) {
            $backendReady = $true
        }
    } catch {
        # Backend not ready yet
    }

    Write-Host "." -NoNewline
}

Write-Host ""

if ($backendReady) {
    Write-Host "✓ Backend is ready!" -ForegroundColor Green
} else {
    Write-Host "⚠ Backend may still be starting. Check the backend window." -ForegroundColor Yellow
}

Write-Host ""

# Check if frontend needs rebuild
Write-Host "[3/6] Checking frontend configuration..." -ForegroundColor Yellow

$offlineLoginFile = "C:\Users\Munji\projects\RessourceMnagerPro\ui\app\src\main\java\com\cuea\rmp\mobile\auth\OfflineTestLogin.kt"
$offlineLoginContent = Get-Content $offlineLoginFile -Raw

if ($offlineLoginContent -match 'const val enabled: Boolean = true') {
    Write-Host "⚠ Offline test login is ENABLED" -ForegroundColor Yellow
    Write-Host "  For real backend testing, it should be disabled." -ForegroundColor Yellow
    Write-Host ""
    $response = Read-Host "Disable offline mode and rebuild? (Y/N)"

    if ($response -eq "Y" -or $response -eq "y") {
        # Disable offline mode
        $offlineLoginContent = $offlineLoginContent -replace 'const val enabled: Boolean = true', 'const val enabled: Boolean = false'
        Set-Content -Path $offlineLoginFile -Value $offlineLoginContent

        Write-Host "[4/6] Rebuilding app with backend connection enabled..." -ForegroundColor Yellow
        $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
        .\gradlew.bat :app:assembleDebug --no-daemon

        if ($LASTEXITCODE -ne 0) {
            Write-Host "✗ Build failed!" -ForegroundColor Red
            exit 1
        }
        Write-Host "✓ Build successful" -ForegroundColor Green
    }
} else {
    Write-Host "✓ Backend connection enabled" -ForegroundColor Green
}

Write-Host ""

# Install app
if ($devices.Count -gt 0) {
    Write-Host "[5/6] Installing app to emulator..." -ForegroundColor Yellow
    $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
    .\gradlew.bat :app:installDebug --no-daemon

    if ($LASTEXITCODE -ne 0) {
        Write-Host "✗ Installation failed!" -ForegroundColor Red
        exit 1
    }

    Write-Host "✓ App installed" -ForegroundColor Green
    Write-Host ""

    # Launch app
    Write-Host "[6/6] Launching app..." -ForegroundColor Yellow
    adb shell am start -n com.cuea.rmp.mobile/.MainActivity
    Write-Host "✓ App launched" -ForegroundColor Green
} else {
    Write-Host "[5/6] Skipping app installation (no emulator)" -ForegroundColor DarkGray
    Write-Host "[6/6] Skipping app launch (no emulator)" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "╔════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║              ✅ SYSTEM READY FOR TESTING               ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

Write-Host "🔐 LOGIN CREDENTIALS:" -ForegroundColor Cyan
Write-Host "   Admin: " -NoNewline; Write-Host "admin@cuea.edu" -ForegroundColor Green
Write-Host "   Password: Check backend window for DataSeeder output" -ForegroundColor DarkGray
Write-Host ""
Write-Host "   OR Register a new account in the app!" -ForegroundColor Yellow
Write-Host ""

Write-Host "📊 MONITORING:" -ForegroundColor Cyan
Write-Host "   Backend: http://localhost:8080" -ForegroundColor White
Write-Host "   Backend logs: Check the backend PowerShell window" -ForegroundColor DarkGray
Write-Host ""

if ($devices.Count -gt 0) {
    $pid = adb shell pidof -s com.cuea.rmp.mobile
    if ($pid) {
        Write-Host "   App logs: adb logcat --pid=$pid" -ForegroundColor DarkGray
    } else {
        Write-Host "   App logs: adb logcat --pid=`$(adb shell pidof -s com.cuea.rmp.mobile)" -ForegroundColor DarkGray
    }
}

Write-Host ""
Write-Host "📖 TESTING GUIDE:" -ForegroundColor Cyan
Write-Host "   See BACKEND_TESTING_GUIDE.md for complete test scenarios" -ForegroundColor White
Write-Host ""

Write-Host "🛑 TO STOP:" -ForegroundColor Cyan
Write-Host "   Close the backend PowerShell window" -ForegroundColor DarkGray
Write-Host "   Close this window" -ForegroundColor DarkGray
Write-Host ""

Write-Host "Happy testing! 🚀" -ForegroundColor Green
Write-Host ""
Write-Host "Press any key to exit this script (backend will keep running)..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

