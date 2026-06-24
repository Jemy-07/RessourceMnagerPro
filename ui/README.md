# Resource Manager Pro - Android UI Module

This module contains the Android client scaffold for Resource Manager Pro.

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
- Regenerated Gradle wrapper (`gradle-wrapper.jar`) if missing

## Quick try

```powershell
cd C:\Users\Munji\projects\RessourceMnagerPro\ui
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

If wrapper jar is missing, open `ui/` in Android Studio first and let it regenerate,
then re-run commands above.

## Notes

- `BuildConfig.BASE_URL` defaults to `http://10.0.2.2:8080/` for emulator + local backend.
- Backend contract source of truth remains `../API_REFERENCE.md`.
- Full feature UI is intentionally out of scope in this pass; foundation layers are in place.

