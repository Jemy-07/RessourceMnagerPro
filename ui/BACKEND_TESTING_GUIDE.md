# Resource Manager Pro - Backend Integration Testing Guide

## 🎯 Purpose

This guide helps you test the complete integration between the Android frontend and Spring Boot backend, including authentication, data sync, and all CRUD operations.

---

## ⚙️ Setup: Enable Backend Connection

### Step 1: Configure the App for Real Backend

Edit `app/src/main/java/com/cuea/rmp/mobile/auth/OfflineTestLogin.kt`:

```kotlin
const val enabled: Boolean = false  // ⚠️ Set to false for backend testing
```

### Step 2: Rebuild the App

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
.\gradlew.bat :app:installDebug
```

### Step 3: Start the Backend

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\api\ResourceManagerPro
.\mvnw.cmd spring-boot:run
```

**Wait for:** `Tomcat started on port 8080` in the logs.

### Step 4: Launch the App

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
adb shell am start -n com.cuea.rmp.mobile/.MainActivity
```

---

## 🧪 Testing Scenarios

### Test 1: User Registration ✅

**Purpose:** Verify new user accounts can be created

1. Open the app on emulator
2. Tap **"Register"** tab
3. Fill in:
   - **Name:** Test User
   - **Email:** test@example.com
   - **Password:** Test1234
   - **Organization Code:** (get from backend DataSeeder logs)
4. Tap **"Register"**
5. **Expected:** Success message, auto-login to main screen

**Backend Verification:**
```powershell
# Check backend logs for:
# "User registered: test@example.com"
```

---

### Test 2: User Login ✅

**Purpose:** Verify authentication with real JWT tokens

1. Logout from current session (if logged in)
2. Tap **"Login"** tab
3. Enter credentials:
   - **Email:** `admin@cuea.edu` (or your registered account)
   - **Password:** (from backend DataSeeder logs)
4. Tap **"Login"**
5. **Expected:** Navigate to main screen with 5 tabs

**Backend Verification:**
```powershell
# Check backend logs for:
# "Login successful: admin@cuea.edu"
# "JWT token generated for user: ..."
```

**What to verify:**
- ✅ Access token stored in DataStore
- ✅ Refresh token stored in DataStore
- ✅ AuthInterceptor adds Bearer token to requests
- ✅ UI shows authenticated state

---

### Test 3: Timesheet Logging (Offline-First) ✅

**Purpose:** Verify offline-first timesheet entry with background sync

**Scenario A: Online Sync**

1. Ensure backend is running and app is online
2. Go to **Timesheets** tab
3. Fill in:
   - **Assignment ID:** 1
   - **Date:** Today's date
   - **Hours:** 8.0
   - **Description:** Backend integration test
4. Tap **"Log Time"**
5. **Expected:** 
   - Entry appears immediately with status `PENDING_SYNC`
   - Within seconds, status changes to `SYNCED` (green)

**Backend Verification:**
```powershell
# Check backend logs for:
# POST /api/v1/timesheets
# "Timesheet logged for assignment 1"
```

**Scenario B: Offline Mode**

1. Turn off WiFi on emulator (or stop backend)
2. Log another timesheet entry
3. **Expected:** Entry saved with status `PENDING_SYNC` (orange)
4. Turn WiFi back on (or restart backend)
5. Pull down to refresh
6. **Expected:** Status changes to `SYNCED` automatically

**What to verify:**
- ✅ Client generates UUID before API call
- ✅ Entry saved to Room database immediately
- ✅ WorkManager queues the sync task
- ✅ Backend accepts UUID from client (idempotent)
- ✅ Duplicate submissions return 409 (handled gracefully)

---

### Test 4: Token Refresh (401 Handling) ✅

**Purpose:** Verify automatic token refresh on expiration

**Manual Test:**

1. Login to the app
2. Wait for access token to expire (15 minutes by default)
3. Navigate to any tab that fetches data (e.g., Resources)
4. **Expected:** 
   - API returns 401 Unauthorized
   - TokenAuthenticator catches it
   - Calls `/auth/refresh` with refresh token
   - Gets new token pair
   - Retries original request
   - UI shows data successfully

**Backend Verification:**
```powershell
# Check backend logs for sequence:
# GET /api/v1/resources → 401 Unauthorized
# POST /auth/refresh → 200 OK (new tokens)
# GET /api/v1/resources → 200 OK (retry succeeds)
```

**Automated Test (Force 401):**

You can temporarily modify `AuthInterceptor` to send an expired token and verify the flow.

**What to verify:**
- ✅ TokenAuthenticator triggered on 401
- ✅ Mutex prevents concurrent refresh calls
- ✅ New tokens saved to DataStore
- ✅ Original request retried with new token
- ✅ No user disruption (seamless)

---

### Test 5: Notifications (Cache-Then-Network) ✅

**Purpose:** Verify cache-first data loading

1. Go to **Notifications** tab
2. **Expected:** 
   - Shows loading spinner briefly
   - Displays notifications from backend
   - Pull down to refresh
   - New notifications appear

**Backend Verification:**
```powershell
# Check backend logs for:
# GET /api/v1/notifications → 200 OK
```

3. Mark a notification as read
4. **Expected:** 
   - Notification marked as read locally
   - Background sync updates backend
   - Refresh shows updated state

**Backend Verification:**
```powershell
# PUT /api/v1/notifications/{id}/read → 200 OK
```

**What to verify:**
- ✅ Data fetched from API
- ✅ Saved to Room database
- ✅ UI observes Room Flow
- ✅ Cache persists across app restarts
- ✅ Pull-to-refresh updates cache

---

### Test 6: Resources List (Pagination) ✅

**Purpose:** Verify resource browsing

1. Go to **Resources** tab
2. **Expected:** List of team members with:
   - Name
   - Email
   - Role
   - Availability status

**Backend Verification:**
```powershell
# GET /api/v1/resources?page=0&size=20 → 200 OK
```

**What to verify:**
- ✅ Resources fetched from backend
- ✅ Cached in Room
- ✅ Displayed in list
- ✅ Pull-to-refresh works

---

### Test 7: Projects List ✅

**Purpose:** Verify project management

1. Go to **Projects** tab
2. **Expected:** List of active projects

**Backend Verification:**
```powershell
# GET /api/v1/projects?page=0&size=20 → 200 OK
```

---

### Test 8: Requests (Approve/Reject) ✅

**Purpose:** Verify allocation request management

1. Go to **Requests** tab
2. **Expected:** List of pending requests
3. Tap **Approve** on a request
4. **Expected:** 
   - Request status updated
   - Success confirmation
   - List refreshes

**Backend Verification:**
```powershell
# PUT /api/v1/requests/{id}/approve → 200 OK
```

**What to verify:**
- ✅ Approve action calls backend
- ✅ Status updated in database
- ✅ UI reflects changes
- ✅ Error handling for failures

---

### Test 9: Logout & Token Cleanup ✅

**Purpose:** Verify secure logout

1. Tap **Logout** button (in top app bar)
2. **Expected:**
   - Calls `/auth/logout` endpoint
   - Clears DataStore tokens
   - Navigates to login screen
   - All cached data cleared (optional)

**Backend Verification:**
```powershell
# POST /auth/logout → 200 OK
# "User logged out: admin@cuea.edu"
```

---

## 🔍 Monitoring & Debugging

### View App Logs

```powershell
# Get app process ID
$pid = adb shell pidof -s com.cuea.rmp.mobile

# Stream logs
adb logcat --pid=$pid
```

**Look for:**
- `[Network]` tags for API calls
- `[Auth]` tags for authentication events
- `[Sync]` tags for WorkManager operations
- `[Room]` tags for database operations

### View Backend Logs

Backend logs show in the terminal where `mvnw spring-boot:run` is running.

**Look for:**
- `POST /auth/login`
- `POST /auth/refresh`
- `POST /api/v1/timesheets`
- `GET /api/v1/resources`
- Error stack traces

### Network Traffic

Use **Android Studio's Network Profiler**:
1. Run > Profile 'app'
2. Select Network tab
3. Observe HTTP requests/responses
4. Verify Authorization headers

---

## 🐛 Common Issues & Solutions

### Issue: "Unable to resolve host 10.0.2.2"

**Cause:** Backend not running or emulator network issue

**Solution:**
```powershell
# Verify backend is running
curl http://localhost:8080/actuator/health

# Check emulator can reach host
adb shell ping 10.0.2.2
```

### Issue: "401 Unauthorized" on all requests

**Cause:** Invalid or expired tokens

**Solution:**
```powershell
# Clear app data
adb shell pm clear com.cuea.rmp.mobile

# Restart app and login again
```

### Issue: "409 TIMESHEET_EXISTS"

**Cause:** Duplicate timesheet entry (normal behavior)

**Solution:** This is expected! Backend is telling us it already has this entry. The app marks it as SYNCED.

### Issue: Notifications not appearing

**Cause:** Backend has no notifications for this user

**Solution:** Create notifications via backend API or admin panel

---

## ✅ Pre-Presentation Checklist

Test EVERYTHING before the demo:

- [ ] Backend running on `http://localhost:8080`
- [ ] App built with `OfflineTestLogin.enabled = false`
- [ ] Can register new account
- [ ] Can login with admin account
- [ ] Can log timesheet (syncs to backend)
- [ ] Notifications load from backend
- [ ] Resources list loads
- [ ] Projects list loads
- [ ] Requests list loads
- [ ] Can approve/reject requests
- [ ] Can logout successfully
- [ ] Token refresh works (wait 15+ min or force expire)

---

## 🎯 Demo Flow (Complete Integration)

**Perfect demo sequence:**

1. **Start:** Show login screen
2. **Register:** Create new account `demo@example.com`
3. **Login:** Auto-login after registration
4. **Timesheets:** Log a work entry, show immediate save + sync
5. **Offline:** Turn off WiFi, log another entry (stays PENDING)
6. **Online:** Turn WiFi back on, show auto-sync
7. **Resources:** Browse team members
8. **Projects:** View active projects
9. **Requests:** Approve an allocation request
10. **Notifications:** Show system alerts
11. **Logout:** Clean exit

**This demonstrates:**
- ✅ Full authentication flow
- ✅ Offline-first architecture
- ✅ Background sync
- ✅ Cache-then-network pattern
- ✅ Real-time data updates
- ✅ Complete feature set

---

## 📞 Need Help?

If something doesn't work:

1. Check backend logs first
2. Check app logs with `adb logcat`
3. Verify network connectivity
4. Clear app data and retry
5. Rebuild app: `.\gradlew.bat clean :app:installDebug`

**Good luck with your presentation! 🚀**

