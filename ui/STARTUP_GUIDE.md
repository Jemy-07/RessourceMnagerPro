# Resource Manager Pro - Android App Startup Guide

## 📋 Prerequisites

Before starting the app, ensure you have:

1. **Android Studio** installed (or Android SDK command-line tools)
2. **Java 17+** (comes with Android Studio)
3. **Android Emulator** configured OR a physical Android device
4. **Backend API** running on `http://localhost:8080` (optional for demo mode)

---

## 🚀 EASIEST WAY: One-Command Start

**For complete backend + frontend testing:**

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
.\start-full-testing.ps1
```

This automated script will:
- ✅ Start the Spring Boot backend
- ✅ Configure the app for backend connection
- ✅ Rebuild if needed
- ✅ Install to emulator
- ✅ Launch the app
- ✅ Show you login credentials

**Then just login and test all features!**

For detailed test scenarios, see: **`BACKEND_TESTING_GUIDE.md`**

---

## 📱 Manual Setup (Step by Step)

### Step 1: Start an Android Emulator

**Option A: Using Android Studio**
1. Open Android Studio
2. Click **Tools** → **Device Manager**
3. Select an emulator (e.g., Pixel 8) and click **Play ▶️**
4. Wait for the emulator to fully boot (shows home screen)

**Option B: Using Command Line**
```powershell
# List available emulators
emulator -list-avds

# Start an emulator (replace with your AVD name)
emulator -avd Pixel_8
```

### Step 2: Install the App

Open PowerShell and navigate to the project directory:

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
```

Then install the app:

```powershell
.\gradlew.bat :app:installDebug
```

**Expected output:** `Installed on 1 device.`

### Step 3: Launch the App

```powershell
adb shell am start -n com.cuea.rmp.mobile/.MainActivity
```

The app should now appear on your emulator/device! 🎉

---

## 🔐 Login Credentials & Backend Connection

### ⚙️ IMPORTANT: Choose Your Testing Mode

The app can work in **two modes**:

#### Mode 1: **Real Backend Testing** (RECOMMENDED FOR FULL TESTING)
- Tests actual API calls, database operations, authentication
- Requires backend running on `http://localhost:8080`
- **Configuration:** In `OfflineTestLogin.kt`, set `enabled = false`

#### Mode 2: **Offline Demo Mode** (UI ONLY)
- Shows UI without backend (no real data)
- Useful for UI/UX demonstrations
- **Configuration:** In `OfflineTestLogin.kt`, set `enabled = true`

---

### 🔑 Credentials for Real Backend Testing

**Step 1: Start the Backend**
```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\api\ResourceManagerPro
.\mvnw.cmd spring-boot:run
```

Wait for: `"Seeded demo data: org=..., admin=admin@cuea.edu"` in logs

**Step 2: Use Seeded Admin Account**
- **Email:** `admin@cuea.edu`
- **Password:** Check backend startup logs (look for DataSeeder output)
- **OR Register a new account** using the Register tab in the app

**Step 3: Test All Features**
- ✅ Login/Logout
- ✅ Register new users
- ✅ Log timesheets (syncs to backend immediately)
- ✅ View notifications from backend
- ✅ Browse resources, projects, requests
- ✅ Approve/reject allocation requests

---

### 🎭 Credentials for Offline Demo Mode

If you set `OfflineTestLogin.enabled = true`:

- **Email:** `tester@rmp.local`
- **Password:** Any non-empty text (e.g., `test123`)

**Note:** In this mode, the app will NOT make any API calls. It's for UI navigation testing only.

---

## 📱 App Features to Demonstrate

Once logged in, you'll see these tabs:

1. **⏱️ Timesheets** - Log work hours (offline-first, syncs automatically)
2. **🔔 Notifications** - View and mark system notifications as read
3. **👥 Resources** - Browse team members and their availability
4. **📂 Projects** - View active projects and assignments
5. **📋 Requests** - Approve/reject resource allocation requests

---

## 🔍 Troubleshooting

### Problem: "No connected devices"

**Solution:**
```powershell
# Check if device is connected
adb devices

# If empty, start an emulator first (see Step 1)
```

### Problem: "App crashes on startup"

**Solution:**
```powershell
# Uninstall old version
adb uninstall com.cuea.rmp.mobile

# Rebuild and reinstall
.\gradlew.bat clean
.\gradlew.bat :app:installDebug
```

### Problem: "Cannot connect to backend"

**Solution:**
1. Check if backend is running on `http://localhost:8080`
2. **OR** Use demo credentials (`tester@rmp.local`) to work offline

### Problem: Gradle build fails

**Solution:**
```powershell
# Set Java home to Android Studio's JDK
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"

# Retry installation
.\gradlew.bat :app:installDebug
```

---

## 📊 Viewing App Logs (Optional)

To debug issues or see what the app is doing:

```powershell
# Clear old logs
adb logcat -c

# Find the app's process ID
$pid = adb shell pidof -s com.cuea.rmp.mobile

# View live logs
adb logcat --pid=$pid
```

---

## 🎯 Presentation Tips

1. **Start with Demo Login** - Use `tester@rmp.local` so you don't depend on backend
2. **Show Offline-First** - Log a timesheet, then show it saves locally even without internet
3. **Navigate Through All Tabs** - Demonstrate each feature screen
4. **Show Pull-to-Refresh** - Swipe down on lists to demonstrate cache updates
5. **Test Multiple Scenarios** - Add timesheets, mark notifications as read, etc.

---

## 📞 Need Help?

Common commands:

```powershell
# Restart the app
adb shell am force-stop com.cuea.rmp.mobile
adb shell am start -n com.cuea.rmp.mobile/.MainActivity

# Check app version
adb shell dumpsys package com.cuea.rmp.mobile | Select-String "versionName"

# Take a screenshot
adb exec-out screencap -p > screenshot.png
```

---

## 📁 Project Structure

```
ui/
├── app/
│   ├── src/main/
│   │   ├── java/com/cuea/rmp/mobile/  # All Kotlin source code
│   │   ├── res/                        # UI resources
│   │   └── AndroidManifest.xml         # App configuration
│   └── build.gradle.kts                # Dependencies
├── STARTUP_GUIDE.md                    # This file
├── BACKEND_TESTING_GUIDE.md            # Complete integration testing guide
├── QUICK_REFERENCE.txt                 # One-page cheat sheet
├── start-app.ps1                       # Quick app installer
└── start-full-testing.ps1              # Backend + Frontend starter
```

---

## 📖 Additional Documentation

- **`BACKEND_TESTING_GUIDE.md`** - Complete guide for testing all API integrations
  - User registration/login flows
  - Timesheet offline-first sync
  - Token refresh mechanism
  - Cache-then-network patterns
  - All CRUD operations with backend

- **`QUICK_REFERENCE.txt`** - One-page quick reference card (print-friendly)

---

## ✅ Pre-Presentation Checklist

### For UI Demo (No Backend)
- [ ] Set `OfflineTestLogin.enabled = true`
- [ ] Emulator is running
- [ ] App installed with `.\start-app.ps1`
- [ ] Test login works (`tester@rmp.local`)

### For Full Integration Demo (With Backend)
- [ ] Set `OfflineTestLogin.enabled = false`
- [ ] Backend running on `http://localhost:8080`
- [ ] App installed with `.\start-full-testing.ps1`
- [ ] Admin login works (`admin@cuea.edu`)
- [ ] Test all features listed in `BACKEND_TESTING_GUIDE.md`

---

**Good luck with your presentation! 🚀**

**For complete testing scenarios, see `BACKEND_TESTING_GUIDE.md`**


