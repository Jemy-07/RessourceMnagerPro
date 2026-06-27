# ✅ Backend Connection Setup - COMPLETED

## What Was Done

### 1. Disabled Offline Test Mode ✅
**File:** `app/src/main/java/com/cuea/rmp/mobile/auth/OfflineTestLogin.kt`

**Changed:** `const val enabled: Boolean = false`

**Why:** This allows the app to make real API calls to the backend instead of using mock credentials with sentinel tokens that block network requests.

---

### 2. Rebuilt the App ✅
**Command:** `.\gradlew.bat :app:assembleDebug`

**Result:** `BUILD SUCCESSFUL in 1m 1s`

**Why:** The new APK now includes the configuration change and can connect to the real backend.

---

### 3. Installed to Emulator ✅
**Command:** `.\gradlew.bat :app:installDebug`

**Result:** `Installed on 1 device.`

**Device:** Pixel_8(AVD) - Android 16

---

### 4. Created Complete Documentation ✅

| Document | Purpose |
|----------|---------|
| `STARTUP_GUIDE.md` | Complete setup instructions for first-time users |
| `BACKEND_TESTING_GUIDE.md` | Comprehensive testing guide with all integration scenarios |
| `QUICK_REFERENCE.txt` | One-page cheat sheet for quick reference during demos |
| `PRESENTERS_GUIDE.md` | **Complete presenter's guide with demo flow and talking points** |
| `start-full-testing.ps1` | Automated script to start backend + frontend together |
| `start-app.ps1` | Simple script to install and launch app |
| `README.md` | Updated project overview with all new documentation |

---

### 5. Created Automated Startup Script ✅
**File:** `start-full-testing.ps1`

**Features:**
- ✅ Automatically starts Spring Boot backend in separate window
- ✅ Waits for backend to be ready (checks health endpoint)
- ✅ Checks if offline mode is enabled
- ✅ Offers to disable offline mode and rebuild if needed
- ✅ Installs app to emulator
- ✅ Launches app automatically
- ✅ Displays login credentials
- ✅ Shows monitoring commands

---

## Current Configuration

### Backend Connection: ✅ ENABLED
```kotlin
// OfflineTestLogin.kt
const val enabled: Boolean = false  // Real backend connection
```

### Backend URL: ✅ CONFIGURED
```kotlin
// build.gradle.kts
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```
- Emulator uses `10.0.2.2:8080` to reach `localhost:8080`
- Perfect for local development

### Latest Build: ✅ INSTALLED
- APK built with backend connection enabled
- Installed on emulator `Pixel_8(AVD)`
- Ready to test immediately

---

## How to Test Right Now

### Option 1: Automated (Easiest)
```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
.\start-full-testing.ps1
```

This will:
1. Start the backend
2. Wait for it to be ready
3. Install the app
4. Launch it
5. Show you the login credentials

### Option 2: Manual
```powershell
# Terminal 1: Start backend
cd C:\Users\Munji\projects\RessourceMnagerPro\api\ResourceManagerPro
.\mvnw.cmd spring-boot:run

# Terminal 2: Launch app
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
adb shell am start -n com.cuea.rmp.mobile/.MainActivity
```

Then login with `admin@cuea.edu` (password in backend logs)

---

## What Can Be Tested Now

### ✅ Full Authentication Flow
- User registration with email/password
- Login with JWT tokens
- Automatic token refresh on 401
- Secure logout with token cleanup

### ✅ Offline-First Timesheet Logging
- Log work hours
- Immediate local save
- Background sync to backend
- Works offline, syncs when online
- Duplicate detection (409 handling)

### ✅ Cache-Then-Network Data Loading
- Notifications: Fetch, cache, mark as read
- Resources: Browse team members
- Projects: View active projects
- Requests: Approve/reject workflows

### ✅ Background Sync
- WorkManager automatic sync
- Pending mutation queue
- Retry logic for failed requests

### ✅ Error Handling
- Structured API error responses
- Network error handling
- Token expiration handling

---

## Login Credentials

### Admin Account (Seeded by Backend)
- **Email:** `admin@cuea.edu`
- **Password:** Check backend startup logs for DataSeeder output

### Register New Account
Use the Register tab in the app to create a new account

### Offline Demo (If Needed)
If you need to demo without backend:
1. Set `OfflineTestLogin.enabled = true`
2. Rebuild: `.\gradlew.bat :app:installDebug`
3. Login: `tester@rmp.local` / `test123`

---

## Verification Checklist

Test these to ensure everything works:

- [ ] Backend starts on `http://localhost:8080`
- [ ] App launches on emulator
- [ ] Can login with `admin@cuea.edu`
- [ ] Can register new account
- [ ] Can log timesheet (shows PENDING_SYNC → SYNCED)
- [ ] Notifications load from backend
- [ ] Resources list loads
- [ ] Projects list loads
- [ ] Requests list loads
- [ ] Can logout successfully

---

## Monitoring Commands

### Check Backend Health
```powershell
curl http://localhost:8080/actuator/health
```

### View App Logs
```powershell
adb logcat --pid=$(adb shell pidof -s com.cuea.rmp.mobile)
```

### Check Connected Devices
```powershell
adb devices
```

### Restart App
```powershell
adb shell am force-stop com.cuea.rmp.mobile
adb shell am start -n com.cuea.rmp.mobile/.MainActivity
```

---

## Architecture Highlights

### Request Flow
```
User Action → ViewModel → Repository → API/Room → Backend
                                              ↓
                                          Room Cache
                                              ↓
                                         UI Update (Flow)
```

### Authentication Flow
```
Login → JWT Tokens → DataStore → AuthInterceptor (adds Bearer token)
                                         ↓
                                    API Request
                                         ↓
                                  401 Unauthorized?
                                         ↓
                              TokenAuthenticator (refresh)
                                         ↓
                                  Retry with new token
```

### Offline-First Timesheet Flow
```
Log Time → Generate UUID → Save to Room (PENDING_SYNC)
                                    ↓
                        Queue in PendingMutationEntity
                                    ↓
                         WorkManager background sync
                                    ↓
                            POST to backend
                                    ↓
                     200/201 → Mark SYNCED (green)
                     409 → Already exists → Mark SYNCED
                     Error → Keep PENDING, retry later
```

---

## Troubleshooting

### "Unable to resolve host 10.0.2.2"
**Solution:** Ensure backend is running on `localhost:8080`

### "401 Unauthorized"
**Solution:** Clear app data and login again
```powershell
adb shell pm clear com.cuea.rmp.mobile
```

### App not making API calls
**Solution:** Verify `OfflineTestLogin.enabled = false` and rebuild

### Backend won't start
**Solution:** Check MariaDB is running and connection credentials are correct

---

## Summary

✅ **Frontend is configured to connect to backend**
✅ **Latest APK is built and installed**
✅ **Complete documentation is ready**
✅ **Automated startup scripts are available**
✅ **App is ready for full integration testing**

**Next Step:** Give this to your presenter with the `PRESENTERS_GUIDE.md` file!

---

## Quick Start for Presenter

**Just run:**
```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
.\start-full-testing.ps1
```

**Then read:** `PRESENTERS_GUIDE.md`

**That's it!** 🚀

