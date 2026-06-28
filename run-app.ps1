# ============================================================
#  Resource Manager Pro - One-Click Run Script
#  Usage:  .\run-app.ps1            (from PowerShell terminal)
#          Double-click run-app.bat (from File Explorer)
# ============================================================

# ── Paths (edit these if your SDK is in a different location) ─
$SDK_ROOT    = "$env:LOCALAPPDATA\Android\Sdk"
$ADB         = "$SDK_ROOT\platform-tools\adb.exe"
$EMULATOR    = "$SDK_ROOT\emulator\emulator.exe"
$JAVA_HOME   = "C:\Program Files\Android\Android Studio\jbr"
$UI_DIR      = "$PSScriptRoot\ui"
$GRADLEW     = "$UI_DIR\gradlew.bat"
$APP_PACKAGE = "com.cuea.rmp.mobile"
$APP_ACTIVITY= "$APP_PACKAGE/.MainActivity"

# ── Banner ────────────────────────────────────────────────────
Clear-Host
Write-Host ""
Write-Host "  +------------------------------------------+" -ForegroundColor Cyan
Write-Host "  |    Resource Manager Pro - Quick Launch   |" -ForegroundColor Cyan
Write-Host "  +------------------------------------------+" -ForegroundColor Cyan
Write-Host ""

function Step($n, $msg) {
    Write-Host "  [$n/5] $msg" -ForegroundColor Yellow
}
function OK($msg)   { Write-Host "        OK  $msg" -ForegroundColor Green }
function FAIL($msg) {
    Write-Host ""
    Write-Host "  [ERROR] $msg" -ForegroundColor Red
    Write-Host ""
    Write-Host "  Press Enter to close..." -ForegroundColor DarkGray
    $null = Read-Host
    exit 1
}

# ── Step 1: Validate tools ────────────────────────────────────
Step 1 "Checking required tools..."

if (-not (Test-Path $ADB))     { FAIL "ADB not found at: $ADB`n         Install Android SDK via Android Studio." }
if (-not (Test-Path $GRADLEW)) { FAIL "gradlew.bat not found at: $GRADLEW" }

OK "ADB and Gradle found"

# ── Step 2: Emulator ──────────────────────────────────────────
Step 2 "Checking for a running emulator..."

$running = & $ADB devices 2>$null | Select-String "emulator.*\bdevice\b"

if ($running.Count -eq 0) {
    Write-Host "        No emulator detected. Starting one..." -ForegroundColor DarkYellow

    if (-not (Test-Path $EMULATOR)) { FAIL "Emulator not found at: $EMULATOR`n         Start an emulator manually from Android Studio." }

    $avdList = & $EMULATOR -list-avds 2>$null | Where-Object { $_.Trim() -ne "" }
    if (-not $avdList) { FAIL "No AVDs found. Create one in Android Studio > Device Manager." }

    $chosenAvd = ($avdList | Select-Object -First 1).Trim()
    Write-Host "        Launching AVD: $chosenAvd" -ForegroundColor Cyan
    Start-Process -FilePath $EMULATOR -ArgumentList "-avd", $chosenAvd -WindowStyle Normal

    Write-Host "        Waiting for emulator to boot (up to 2 minutes)..." -ForegroundColor DarkGray

    $booted   = $false
    $maxWait  = 120
    $interval = 6
    $elapsed  = 0

    while ($elapsed -lt $maxWait) {
        Start-Sleep -Seconds $interval
        $elapsed += $interval
        $status = & $ADB shell getprop sys.boot_completed 2>$null
        if ($status -and $status.Trim() -eq "1") {
            $booted = $true
            break
        }
        Write-Host "        ... still booting ($elapsed / $maxWait s)" -ForegroundColor DarkGray
    }

    if (-not $booted) { FAIL "Emulator did not boot in time.`n         Run the script again once the emulator window is fully loaded." }
}

OK "Emulator is ready"

# ── Step 3: Java ──────────────────────────────────────────────
Step 3 "Configuring Java environment..."
$env:JAVA_HOME = $JAVA_HOME
OK "JAVA_HOME = $JAVA_HOME"

# ── Step 4: Build and install ─────────────────────────────────
Step 4 "Building and installing app (first run may take ~2 min)..."
Write-Host ""

Push-Location $UI_DIR

& $GRADLEW ":app:installDebug" "--daemon"
$exitCode = $LASTEXITCODE

Pop-Location

if ($exitCode -ne 0) {
    FAIL "Build failed (Gradle exit code $exitCode).`n         Run '.\ui\gradlew.bat :app:installDebug' for full error output."
}

Write-Host ""
OK "App built and installed"

# ── Step 5: Launch ────────────────────────────────────────────
Step 5 "Launching app on emulator..."

$null = & $ADB shell am start -n $APP_ACTIVITY 2>$null
Start-Sleep -Seconds 1

OK "App is running!"

# ── Done ──────────────────────────────────────────────────────
Write-Host ""
Write-Host "  +------------------------------------------+" -ForegroundColor Cyan
Write-Host "  |            App is now running!           |" -ForegroundColor Cyan
Write-Host "  +------------------------------------------+" -ForegroundColor Cyan
Write-Host "  |  Email:    tester@rmp.local              |" -ForegroundColor White
Write-Host "  |  Password: test123                       |" -ForegroundColor White
Write-Host "  +------------------------------------------+" -ForegroundColor Cyan
Write-Host ""
Write-Host "  Check the emulator window." -ForegroundColor Green
Write-Host ""
