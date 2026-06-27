# Resource Manager Pro - Android UI Module

Enterprise resource management mobile application built with Jetpack Compose, Retrofit, Room, and Hilt.

---

## 🚀 Quick Start

### **For Complete Backend + Frontend Testing**

```powershell
.\start-full-testing.ps1
```

This will:
- ✅ Start Spring Boot backend automatically
- ✅ Configure app for real API connection
- ✅ Install to emulator
- ✅ Launch the app

**Login:** `admin@cuea.edu` (password shown in backend logs)

### **For UI-Only Demo (No Backend)**

1. Set `OfflineTestLogin.enabled = true` in `OfflineTestLogin.kt`
2. Run: `.\start-app.ps1`
3. **Login:** `tester@rmp.local` / `test123`

---

## 📚 Documentation Guide

| File | Use Case |
|------|----------|
| **`STARTUP_GUIDE.md`** | Complete setup instructions for first-time setup |
| **`BACKEND_TESTING_GUIDE.md`** | Comprehensive API integration testing guide with all test scenarios |
| **`QUICK_REFERENCE.txt`** | One-page cheat sheet for presentations |
| **`start-full-testing.ps1`** | Automated script to start backend + frontend together |
| **`start-app.ps1`** | Simple script to install and launch app only |

**👉 For your presentation:** Start with `STARTUP_GUIDE.md`, then use `start-full-testing.ps1`

---

## What is implemented

- Hilt application setup (`RmpApplication`) and Compose activity shell (`MainActivity`).
- Minimal usable UI flow:
  - `AuthScreen` for login/register
  - `TimesheetScreen` for logging time, manual sync, and local queue visibility
  - `NotificationScreen` for refresh + mark-read
  - `ResourceScreen` for cache-then-network resource listing
  - `ProjectScreen` for cache-then-network project listing
  - `RequestScreen` for list/refresh and approve/reject actions
  - session-gated root composable (`AppRoot`)
- Retrofit + kotlinx serialization network layer with:
  - API envelope model (`ApiResponse<T>`)
  - error parsing + exception mapping
  - JWT auth interceptor
  - token refresh authenticator with serialized refresh handling
- DataStore-backed token storage (`TokenManager`).
- Retrofit APIs + DTOs for auth, users, resources/skills, projects/assignments,
  requests, timesheets, budgets, notifications, and device token registration.
- Room database with:
  - `PendingMutationEntity` queue for offline mutations
  - local timesheet entity and DAO
  - local notification entity and DAO
  - local resource entity and DAO
  - local project entity and DAO
  - local request entity and DAO
- Offline-first timesheet repository:
  - creates client UUIDs
  - stores pending entries locally
  - replays pending logs
  - treats `TIMESHEET_EXISTS` as sync success
- WorkManager sync worker/scheduler wired through Hilt.
- Basic unit test for API error parsing.

## Prerequisites

- Android Studio with Gradle JDK set to 17+
- Android SDK for API 34
- Android Emulator or physical device
- (Optional) Backend running on `http://localhost:8080` for full integration testing

---

## 🎯 Testing Modes

### Real Backend Mode (RECOMMENDED)
Set `OfflineTestLogin.enabled = false` to test:
- ✅ User registration & login with JWT tokens
- ✅ Automatic token refresh on 401
- ✅ Offline-first timesheet sync
- ✅ Cache-then-network data patterns
- ✅ All CRUD operations with backend
- ✅ Background WorkManager sync

### Offline Demo Mode
Set `OfflineTestLogin.enabled = true` for:
- ✅ UI navigation without backend
- ✅ Demo presentations
- ❌ No real data persistence
- ❌ No API calls (blocked by sentinel tokens)

---

## 🧪 Build & Test Commands

### Run Unit Tests
```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Build Debug APK
```powershell
.\gradlew.bat :app:assembleDebug
```

### Install to Emulator
```powershell
.\gradlew.bat :app:installDebug
```

### Launch App
```powershell
adb shell am start -n com.cuea.rmp.mobile/.MainActivity
```

### View App Logs
```powershell
adb logcat --pid=$(adb shell pidof -s com.cuea.rmp.mobile)
```

---

## 🏗️ Architecture Highlights

### Offline-First Timesheet Sync
- Client generates UUID before API call
- Immediate save to Room database
- Background sync via WorkManager
- Idempotent with backend `TIMESHEET_EXISTS` handling
- Status tracking: `PENDING_SYNC` → `SYNCED`

### JWT Authentication
- `AuthInterceptor` adds Bearer token to all requests
- `TokenAuthenticator` handles 401 with automatic refresh
- Mutex-protected refresh to prevent concurrent token burns
- DataStore for secure token storage

### Cache-Then-Network Pattern
- Room is single source of truth for UI
- API fetch → Room replace → UI observes Flow
- Works for: Resources, Projects, Requests, Notifications

### Dependency Injection
- Hilt for all components
- ViewModel injection in Compose
- Repository pattern with clean separation

---

## 📦 Tech Stack

- **UI:** Jetpack Compose, Material 3
- **Networking:** Retrofit, OkHttp, kotlinx.serialization
- **Database:** Room with Flow-based reactive queries
- **Background:** WorkManager with Hilt integration
- **Storage:** DataStore for tokens
- **DI:** Hilt
- **Architecture:** MVVM, Repository Pattern

---

## 🔧 Configuration

### Backend URL
Edit `app/build.gradle.kts`:
```kotlin
buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080/\"")
```
- **Emulator:** Use `10.0.2.2:8080` (host loopback)
- **Physical Device:** Use your computer's IP (e.g., `192.168.1.100:8080`)

### Offline Test Mode Toggle
Edit `app/src/main/java/com/cuea/rmp/mobile/auth/OfflineTestLogin.kt`:
```kotlin
const val enabled: Boolean = false  // false = real backend, true = UI demo
```

---

## 🐛 Troubleshooting

### Build fails with "Unable to access jarfile"
```powershell
# Open project in Android Studio to regenerate wrapper
# OR use the existing wrapper
```

### App won't connect to backend
```powershell
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check emulator can reach host
adb shell ping 10.0.2.2
```

### Clear app data and retry
```powershell
adb shell pm clear com.cuea.rmp.mobile
```

### Gradle JDK issues
```powershell
$env:JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat clean
```

---

## 📞 Support & Testing

- **Complete testing guide:** See `BACKEND_TESTING_GUIDE.md`
- **Quick reference:** See `QUICK_REFERENCE.txt`
- **Setup help:** See `STARTUP_GUIDE.md`

---

## Notes

- `BuildConfig.BASE_URL` defaults to `http://10.0.2.2:8080/` for emulator + local backend
- Backend connection can be toggled via `OfflineTestLogin.enabled` flag
- **For presentation:** Use `start-full-testing.ps1` for complete backend integration testing
- **For UI demo:** Set `OfflineTestLogin.enabled = true` and use test credentials
- Backend contract documented in `../api/API_REFERENCE.md` (if available)
- All API responses use `ApiResponse<T>` envelope wrapper

---

**Ready to test? Run `.\start-full-testing.ps1` to start both backend and frontend!** 🚀
