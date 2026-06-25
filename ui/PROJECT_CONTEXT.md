# Resource Manager Pro — Android Module: Handoff Context

This file is the handoff for continuing this Android module **inside Android
Studio** (this sandbox has no Android SDK and only Java 8, so the build below
has been authored but never actually compiled/verified — treat first sync in
Android Studio as the real first build). It's meant to be read by you or by
Claude Code running inside the Android Studio project.

The authoritative source for *what the backend actually does* is
[`../API_REFERENCE.md`](../API_REFERENCE.md) (repo root) — it was produced by
reading every line of the Spring Boot backend (`api/ResourceManagerPro`), not
by inference. Everything below is derived from it. **If anything here ever
seems to conflict with that file, the file wins** — re-derive from it rather
than from memory of this summary.

---

## 1. What exists on disk right now

```
ui/
  settings.gradle.kts         — includes ":app", google()/mavenCentral() repos configured
  build.gradle.kts            — root: plugin version declarations (AGP 8.5.2, Kotlin 2.0.20, Hilt 2.51.1, KSP)
  gradle.properties           — AndroidX + nonTransitiveRClass
  gradle/wrapper/gradle-wrapper.properties  — points at Gradle 8.7
  gradlew, gradlew.bat         — wrapper scripts (pre-existing)
  app/
    build.gradle.kts          — app module: Compose, Retrofit+kotlinx.serialization, Room+KSP,
                                 WorkManager+Hilt-Work, Hilt DI, DataStore, kotlinx-datetime
    src/main/AndroidManifest.xml  — INTERNET permission, RmpApplication, MainActivity, WorkManager
                                    foreground-service merge entry
```

**Nothing under `app/src/main/java` exists yet** — no Kotlin source at all
(no `RmpApplication.kt`, no `MainActivity.kt`, no networking/DB code). The
manifest references classes that don't exist yet (`.RmpApplication`,
`.MainActivity`, theme `Theme.RmpMobile`) — **the project will not sync/build
until those are added.** That's the very next step.

### ⚠️ Known gap: `gradle/wrapper/gradle-wrapper.jar` is missing
Only `gradle-wrapper.properties` was created (text). The actual wrapper jar
is a binary file this tooling can't author. **First thing to do in Android
Studio**: open the `ui/` folder as a project — Android Studio detects the
missing/stale wrapper and will offer to regenerate it (or run `gradle wrapper`
from a system-installed Gradle once, if you have one). Don't hand-build that
jar.

### ⚠️ Toolchain requirement
The app module targets Java 17 (`compileOptions`/`kotlinOptions` =
`VERSION_17`/`"17"`) because AGP 8.5.x + Compose require it. Make sure Android
Studio's Gradle JDK (Settings → Build Tools → Gradle) is set to 17+, not 8.

---

## 2. Target architecture (agreed plan, not yet all built)

Stack: **Kotlin, Jetpack Compose, Retrofit + kotlinx.serialization, Room,
WorkManager, Hilt, DataStore** (for token storage). Package root:
`com.cuea.rmp.mobile`.

```
com.cuea.rmp.mobile/
  RmpApplication.kt              — @HiltAndroidApp
  MainActivity.kt                — Compose entry point (minimal shell — UI is out of scope for this pass)
  core/
    network/
      ApiResponse.kt              — generic envelope {success, message, data, errorCode, timestamp}
      ApiException.kt             — thrown by a safeApiCall() helper; carries errorCode + fieldErrors map
      AuthInterceptor.kt          — attaches "Authorization: Bearer <access>" from TokenManager
      TokenAuthenticator.kt       — OkHttp Authenticator: on 401, calls /auth/refresh once, retries, else forces logout
      NetworkModule.kt            — Hilt: OkHttpClient, Retrofit, Json instance, per-feature Api instances
    db/
      AppDatabase.kt              — Room database, version 1
      PendingMutationEntity.kt    — generic offline-write queue (see §5)
      Converters.kt               — Room TypeConverters for kotlinx.datetime types
  auth/
    AuthApi.kt, dto/AuthDtos.kt   — register/login/refresh/logout
    TokenManager.kt               — DataStore-backed access/refresh token storage
    AuthRepository.kt
  user/                           — UserApi + dto (ADMIN-only user management)
  resource/                       — ResourceApi + SkillApi + dto (+ Room cache entities)
  project/                        — ProjectApi + AssignmentApi + dto (+ Room cache entities)
  request/                        — RequestApi + dto (+ Room cache entity)
  timesheet/                      — TimesheetApi + dto + Room entity + offline-first TimesheetRepository (see §5 — this is the one truly offline-capable flow per the backend)
  budget/                         — BudgetApi + dto
  notification/                   — NotificationApi + DeviceApi + dto
  sync/
    SyncWorker.kt                 — WorkManager CoroutineWorker: drains PendingMutationEntity queue, refreshes read caches
    SyncScheduler.kt               — enqueues periodic + on-network-available work
  di/
    DatabaseModule.kt, RepositoryModule.kt
```

Only the Gradle/manifest layer above has been created so far; everything in
this tree is the next-session work plan, not yet on disk.

---

## 3. Condensed API surface (full detail lives in `API_REFERENCE.md`)

Base URL in dev: `http://10.0.2.2:8080/` (already wired into
`BuildConfig.BASE_URL` in `app/build.gradle.kts` — `10.0.2.2` is the
emulator's alias for the host machine's `localhost`; change it for a physical
device or staging backend).

All paths below are relative to `BASE_URL` and already include `api/v1/...`.
Every response is wrapped in `ApiResponse<T>`; every error uses the same
envelope with `success:false`.

| Feature | Base path | Notes |
|---|---|---|
| Auth | `api/v1/auth/{register,login,refresh,logout}` | JWT, 15 min access / 14 day refresh, rotates on refresh |
| Users | `api/v1/users` | ADMIN only; paginated list |
| Resources | `api/v1/resources`, `.../skills`, `.../availability`, `.../match` | paginated list; availability/match ignore assignment overlap (status+time-off only) |
| Skills | `api/v1/skills` | unpaged |
| Projects | `api/v1/projects`, `.../assignments` | paginated projects; unpaged assignments-by-project |
| Assignments | `api/v1/assignments/{id}` | conflict-checked (100% allocation cap) on create/update |
| Requests | `api/v1/requests`, `.../approve`, `.../reject` | unpaged; approve creates an Assignment transactionally |
| Timesheets | `api/v1/timesheets`, `.../submit`, `.../approve` | **client-supplied `id` (UUID)** — the only offline-idempotent-by-design endpoint; duplicate POST → 409 `TIMESHEET_EXISTS` (treat as success, not error) |
| Budgets | `api/v1/projects/{projectId}/budget` | ADMIN/MANAGER only, one per project |
| Notifications | `api/v1/notifications`, `.../read` | unpaged, scoped to caller |
| Devices | `api/v1/devices/token` | FCM token registration |

Status codes: 200/201 success, 400 generic domain error, 401 auth, 403 RBAC,
404 not found, 409 conflict, 422 business-rule violation, 500 unmapped
(includes optimistic-lock conflicts — there's no client-visible `version`
field anywhere, so don't build conflict-resolution UI around one).

### Hard constraints this client must respect (don't relitigate these — they're backend facts, not preferences)
- **No delta sync.** No `updatedSince` param, no changes feed, no usable
  `version`/ETag in any response. Local cache refresh = re-fetch full list(s)
  and diff-replace into Room. Don't design a sync engine that expects the
  server to tell it what changed.
- **No batch endpoints.** Every offline-queued write is replayed as its own
  individual HTTP call.
- **Only Timesheet-log is safely retryable** (client UUID + 409-as-success).
  Every other mutation (Project/Resource/Assignment/Request create, etc.) has
  **no dedupe on the server** — a naive "retry on reconnect" will create
  duplicates. The local `PendingMutationEntity` queue needs its own
  has-this-already-succeeded bookkeeping (e.g. mark a row "in-flight, awaiting
  ack" and don't re-enqueue it blindly on every sync pass) for anything that
  isn't a timesheet log.
- **No reports/exports/WebSocket/SSE/rate-limiting/CORS** exist server-side.
  Don't build client code that expects any of them.
- **TimeOff has no HTTP endpoint at all** (domain/DB exist, no controller) —
  don't build a "request time off" screen against this backend yet; it would
  need a new backend endpoint first.

---

## 4. Auth flow (for `AuthInterceptor`/`TokenAuthenticator`)

1. Login/register → store `accessToken` + `refreshToken` in
   `TokenManager` (DataStore Preferences, not SharedPreferences — survives
   process death cleanly and is the modern recommendation).
2. Every authenticated request: `AuthInterceptor` reads the current access
   token from `TokenManager` synchronously (DataStore needs a blocking read
   here, e.g. `runBlocking { tokenManager.accessToken.first() }`, since OkHttp
   interceptors aren't suspend-friendly — accepted tradeoff) and adds
   `Authorization: Bearer <token>`.
3. On a `401` response, OkHttp's `Authenticator` (not interceptor) fires:
   call `/auth/refresh` with the stored refresh token, persist the **new**
   rotated pair, retry the original request once. If refresh itself fails
   (`INVALID_REFRESH_TOKEN`), clear tokens and surface a "logged out" state to
   the UI layer — don't loop.
4. Synchronize refresh across concurrent in-flight requests (a mutex/lock
   around the refresh call) so 5 simultaneous 401s don't fire 5 refresh calls
   and burn through rotation (each refresh invalidates the previous refresh
   token server-side).

---

## 5. Sync design (concrete plan for `sync/` + `timesheet/`)

**Reads** (Resources, Projects, Assignments, Requests, Notifications, Budgets):
cache-then-network. Room is the single source of truth for the UI (Flow from
DAO); a repository function fetches the relevant list/get endpoint, replaces
the corresponding Room rows in a transaction, and the UI observes Room — it
never reads the network response directly. Triggered manually (pull-to-refresh)
and periodically via `SyncWorker`.

**Writes** — two tiers:
- **Timesheet log** (`TimesheetRepository.logTime`): generate the `id` UUID
  client-side at creation time, insert into Room as `PENDING_SYNC` immediately
  (instant UI feedback), enqueue a `PendingMutationEntity`, then `SyncWorker`
  (or an immediate best-effort attempt if online) POSTs it. On `409
  TIMESHEET_EXISTS`, treat as success and mark the local row synced — this is
  the one flow the backend explicitly supports offline-safe.
- **Everything else** (Resource/Project/Assignment/Request create/update,
  approve/reject, budget allocate): these have no server-side idempotency, so
  the safest design is **don't silently auto-replay them from a background
  queue** — surface "couldn't reach server, retry?" in the UI for these
  actions rather than guaranteeing eventual delivery via WorkManager the way
  timesheets get it. If a queue is wanted anyway for these, store an
  app-generated `attemptId` locally just for log/dedupe purposes (the server
  won't understand it), and mark the row `FAILED_NEEDS_USER_ACTION` rather
  than retrying forever, to avoid duplicate Projects/Assignments piling up
  server-side from blind retries.

`PendingMutationEntity` (generic queue row) — suggested shape:
```kotlin
@Entity
data class PendingMutationEntity(
    @PrimaryKey val localId: String,        // app-generated, not server id
    val entityType: String,                 // "TIMESHEET", "ASSIGNMENT", ...
    val httpMethod: String,
    val path: String,
    val bodyJson: String,
    val createdAt: Long,
    val status: String,                     // PENDING | IN_FLIGHT | SYNCED | FAILED
    val lastError: String? = null
)
```

---

## 6. Next steps, in order

1. Open `ui/` in Android Studio, let it regenerate the Gradle wrapper jar, set
   Gradle JDK to 17+, sync.
2. Add the missing Kotlin source the manifest already references:
   `RmpApplication.kt` (`@HiltAndroidApp`), `MainActivity.kt` (minimal Compose
   scaffold + `Theme.RmpMobile`/`values/themes.xml`) — needed just to get a
   green build before adding real features.
3. Build `core/network` (ApiResponse, ApiException, interceptors, Hilt
   `NetworkModule`) per §4.
4. Build the per-feature Retrofit API interfaces + `@Serializable` DTOs per
   §3/`API_REFERENCE.md`, one feature package at a time — Auth first (needed
   by everything else), then Timesheet (the offline-critical one), then the
   rest.
5. Build `core/db` (`AppDatabase`, entities, DAOs) and the
   `PendingMutationEntity` queue per §5.
6. Build `sync/SyncWorker` + `SyncScheduler`, wire into `RmpApplication` via
   Hilt-Work.
7. First real Gradle build (`./gradlew :app:assembleDebug`) — this sandbox
   couldn't run this (no Android SDK, Java 8 only), so this will be the actual
   first compile check.
