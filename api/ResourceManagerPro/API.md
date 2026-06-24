# Resource Manager Pro — API Reference

Backend REST API for the UI team. Base URL (local): `http://localhost:8080`

All paths are versioned under `/api/v1`.

---

## 1. Conventions

### Response envelope
**Every** endpoint returns this envelope (`ApiResponse<T>`):

```jsonc
{
  "success": true,
  "message": "Human-readable message (may be null)",
  "data": { /* payload T, or null */ },
  "errorCode": null,            // machine code on failure
  "timestamp": "2026-06-24T08:13:56.307Z"
}
```

On error:
```jsonc
{
  "success": false,
  "message": "Resource 42 not found",
  "data": null,
  "errorCode": "NOT_FOUND",
  "timestamp": "2026-06-24T08:13:56.307Z"
}
```

Validation errors put a field→message map in `data`:
```jsonc
{
  "success": false,
  "message": "Validation failed",
  "data": { "email": "email must be a valid address", "password": "password must be 8-100 characters" },
  "errorCode": "VALIDATION_ERROR",
  "timestamp": "..."
}
```

### Auth
- Obtain tokens via `POST /api/v1/auth/login`.
- Send the access token on every protected call: `Authorization: Bearer <accessToken>`.
- Access token TTL: **15 min**. Refresh token TTL: **14 days**. Use `POST /api/v1/auth/refresh` to rotate (the old refresh token is revoked on use).
- `Content-Type: application/json` for all bodies.

### HTTP status codes
| Status | Meaning | `errorCode` examples |
|--------|---------|----------------------|
| 200 | OK | — |
| 201 | Created | — |
| 400 | Validation failed | `VALIDATION_ERROR` |
| 401 | Not authenticated / bad token | `UNAUTHENTICATED`, `INVALID_CREDENTIALS`, `INVALID_TOKEN` |
| 403 | Authenticated but wrong role | `FORBIDDEN` |
| 404 | Not found | `NOT_FOUND` |
| 409 | Conflict / invalid state | `CONFLICT`, `EMAIL_ALREADY_EXISTS`, `ASSIGNMENT_CONFLICT`, `REQUEST_ALREADY_DECIDED`, `INVALID_TIMESHEET_STATE`, `TIMESHEET_EXISTS` |
| 422 | Business rule violation | `BUSINESS_RULE_VIOLATION`, `INVALID_*` |

### Roles (RBAC)
`ADMIN`, `MANAGER`, `APPROVER`, `MEMBER`. ADMIN is a superset (allowed wherever MANAGER/APPROVER are).

### Dates & numbers
- Dates: ISO `yyyy-MM-dd` (e.g. `2026-08-01`).
- Timestamps: ISO-8601 UTC (e.g. `2026-06-24T08:13:56.307Z`).
- Money amounts: JSON numbers (e.g. `75.50`); `currency` is a 3-letter code (e.g. `"USD"`).

### Enums
| Enum | Values |
|------|--------|
| Role | `ADMIN`, `MANAGER`, `APPROVER`, `MEMBER` |
| ResourceType | `HUMAN`, `EQUIPMENT`, `FACILITY`, `MATERIAL` |
| AvailabilityStatus | `AVAILABLE`, `UNAVAILABLE` |
| ProjectStatus | `PLANNED`, `ACTIVE`, `ON_HOLD`, `DONE` |
| AssignmentStatus | `TODO`, `IN_PROGRESS`, `DONE` |
| RequestStatus | `PENDING`, `APPROVED`, `REJECTED` |
| TimesheetStatus | `DRAFT`, `SUBMITTED`, `APPROVED` |
| NotificationType | `CONFLICT`, `APPROVAL`, `DEADLINE` |
| Platform | `ANDROID`, `IOS`, `WEB` |

### Paged responses (`PageResult<T>`)
```jsonc
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 42, "totalPages": 3 }
```

### Public (no token) endpoints
`/api/v1/auth/**`, `/actuator/health`, `/swagger-ui.html`, `/v3/api-docs/**`. Everything else requires a valid token.

---

## 2. Auth — `/api/v1/auth`

### POST `/register` · public
Self-registration; always creates a `MEMBER`.
```jsonc
// request
{ "orgId": "uuid", "fullName": "Jane Doe", "email": "jane@cuea.edu", "password": "Secret123" }
// 201 data
{ "userId": "uuid", "email": "jane@cuea.edu", "role": "MEMBER" }
```

### POST `/login` · public
```jsonc
// request
{ "email": "jane@cuea.edu", "password": "Secret123" }
// 200 data
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 900 }
```

### POST `/refresh` · public
```jsonc
{ "refreshToken": "eyJ..." }   // -> 200 data: same shape as login (new pair; old refresh revoked)
```

### POST `/logout` · public
```jsonc
{ "refreshToken": "eyJ..." }   // -> 200 data: null
```

---

## 3. Users — `/api/v1/users` · **ADMIN only**

`UserResponse`: `{ id, orgId, fullName, email, role, active }`

| Method | Path | Body | Result |
|--------|------|------|--------|
| POST | `/api/v1/users` | `{ orgId, fullName, email, password, role }` | 201 `UserResponse` |
| GET | `/api/v1/users/{id}` | — | `UserResponse` |
| GET | `/api/v1/users?page=0&size=20` | — | `PageResult<UserResponse>` |
| PUT | `/api/v1/users/{id}` | `{ fullName, role }` | `UserResponse` |
| POST | `/api/v1/users/{id}/deactivate` | — | `null` (sets `active=false`) |

Notes: `email` must be unique; duplicate → 409 `EMAIL_ALREADY_EXISTS`. `password` 8–100 chars (stored hashed; never returned).

---

## 4. Skills — `/api/v1/skills`

`SkillResponse`: `{ id, orgId, name }`

| Method | Path | Role | Body | Result |
|--------|------|------|------|--------|
| POST | `/api/v1/skills` | MANAGER/ADMIN | `{ orgId, name }` | 201 `SkillResponse` |
| GET | `/api/v1/skills` | any authenticated | — | `SkillResponse[]` |

---

## 5. Resources — `/api/v1/resources`

`ResourceResponse`:
```jsonc
{
  "id": "uuid", "orgId": "uuid", "userId": "uuid|null",
  "name": "Alice (Engineer)", "type": "HUMAN",
  "hourlyRateAmount": 75.50, "currency": "USD",
  "availabilityStatus": "AVAILABLE",
  "skills": [ { "skillId": "uuid", "proficiency": 5 } ]
}
```

| Method | Path | Role | Body / Query | Result |
|--------|------|------|--------------|--------|
| POST | `/api/v1/resources` | MANAGER/ADMIN | `{ orgId, userId?, name, type, hourlyRateAmount, currency }` | 201 `ResourceResponse` |
| GET | `/api/v1/resources/{id}` | authenticated | — | `ResourceResponse` |
| GET | `/api/v1/resources?page=0&size=20` | authenticated | — | `PageResult<ResourceResponse>` |
| PUT | `/api/v1/resources/{id}` | MANAGER/ADMIN | `{ name, hourlyRateAmount, currency, availabilityStatus }` | `ResourceResponse` |
| DELETE | `/api/v1/resources/{id}` | MANAGER/ADMIN | — | `null` (soft delete) |
| POST | `/api/v1/resources/{id}/skills` | MANAGER/ADMIN | `{ skillId, proficiency }` (1–5) | `ResourceResponse` |
| GET | `/api/v1/resources/{id}/availability?from=&to=` | authenticated | dates | `AvailabilityResponse` |
| GET | `/api/v1/resources/match?skillId=&from=&to=` | authenticated | uuid + dates | `ResourceMatchResponse[]` |

`AvailabilityResponse`: `{ resourceId, from, to, available, reason }`
`ResourceMatchResponse`: `{ resourceId, name, type, proficiency, hourlyRateAmount, currency }` — only available resources holding the skill, **ordered by proficiency desc**.

Notes: adding a skill already present → 409 `SKILL_ALREADY_ASSIGNED`; `proficiency` outside 1–5 → 400. Availability reflects `availabilityStatus` + approved time-off (booking-level capacity is enforced at assignment time, see §6).

---

## 6. Projects & Assignments

### Projects — `/api/v1/projects`
`ProjectResponse`: `{ id, orgId, managerId, name, description, startDate, endDate, status }`

| Method | Path | Role | Body / Query | Result |
|--------|------|------|--------------|--------|
| POST | `/api/v1/projects` | MANAGER/ADMIN | `{ orgId, managerId, name, description?, startDate, endDate }` | 201 `ProjectResponse` (status `PLANNED`) |
| GET | `/api/v1/projects/{id}` | authenticated | — | `ProjectResponse` |
| GET | `/api/v1/projects?page=0&size=20` | authenticated | — | `PageResult<ProjectResponse>` |
| PUT | `/api/v1/projects/{id}` | MANAGER/ADMIN | `{ name, description?, startDate, endDate, status }` | `ProjectResponse` |
| DELETE | `/api/v1/projects/{id}` | MANAGER/ADMIN | — | `null` (soft delete) |
| POST | `/api/v1/projects/{id}/assignments` | MANAGER/ADMIN | `{ resourceId, title, startDate, endDate, allocationPct }` | 201 `AssignmentResponse` |
| GET | `/api/v1/projects/{id}/assignments` | authenticated | — | `AssignmentResponse[]` |

### Assignments — `/api/v1/assignments`
`AssignmentResponse`: `{ id, projectId, resourceId, title, startDate, endDate, allocationPct, status }`

| Method | Path | Role | Body | Result |
|--------|------|------|------|--------|
| GET | `/api/v1/assignments/{id}` | authenticated | — | `AssignmentResponse` |
| PUT | `/api/v1/assignments/{id}` | MANAGER/ADMIN | `{ startDate, endDate, allocationPct, status? }` | `AssignmentResponse` (reschedule) |

Notes: `allocationPct` 0–100. Creating/rescheduling an assignment that pushes a resource's overlapping allocations **over 100%** (or onto an unavailable/time-off window) → 409 `ASSIGNMENT_CONFLICT`. New assignments start `TODO`.

---

## 7. Budget — `/api/v1/projects/{id}/budget` · **MANAGER/ADMIN only**

`BudgetResponse`:
```jsonc
{
  "id": "uuid", "projectId": "uuid", "currency": "USD",
  "totalAmount": 10000.0, "allocatedAmount": 5000.0, "spentAmount": 570.0,
  "margin": 9430.0,      // total − spent
  "remaining": 5000.0    // total − allocated
}
```

| Method | Path | Body | Result |
|--------|------|------|--------|
| GET | `/api/v1/projects/{id}/budget` | — | `BudgetResponse` (404 if none set) |
| PUT | `/api/v1/projects/{id}/budget` | `{ totalAmount, allocatedAmount, currency }` | `BudgetResponse` (creates or updates) |

`spentAmount` is computed automatically (Σ approved-timesheet hours × resource hourlyRate) and updates when a timesheet is approved — the UI just re-fetches the budget.

---

## 8. Requests (approval workflow) — `/api/v1/requests`

`RequestResponse`:
```jsonc
{
  "id": "uuid", "requesterId": "uuid", "approverId": "uuid|null",
  "resourceId": "uuid", "projectId": "uuid", "title": "Need Carol",
  "startDate": "2026-08-01", "endDate": "2026-08-10", "allocationPct": 50,
  "status": "PENDING", "comments": null, "decidedAt": null
}
```

| Method | Path | Role | Body / Query | Result |
|--------|------|------|--------------|--------|
| POST | `/api/v1/requests` | authenticated (raise) | `{ resourceId, projectId, title, startDate, endDate, allocationPct }` | 201 `RequestResponse` (`PENDING`) |
| GET | `/api/v1/requests?status=PENDING` | authenticated | `status?` (optional filter) | `RequestResponse[]` |
| POST | `/api/v1/requests/{id}/approve` | APPROVER/ADMIN | — | `RequestResponse` (`APPROVED`) |
| POST | `/api/v1/requests/{id}/reject` | APPROVER/ADMIN | `{ comments }` | `RequestResponse` (`REJECTED`) |

Notes:
- `requesterId` is taken from the token on create; `approverId` from the token on approve/reject (not sent in the body).
- Create runs an availability check up-front (unavailable resource → 409 `RESOURCE_UNAVAILABLE`).
- **Approve creates the assignment** automatically; if that conflicts (over-allocation) → 409 and the request stays `PENDING`.
- A second decision on an already-decided request → 409 `REQUEST_ALREADY_DECIDED`.

---

## 9. Timesheets — `/api/v1/timesheets`

`TimesheetResponse`: `{ id, resourceId, assignmentId, workDate, hours, status }`

| Method | Path | Role | Body | Result |
|--------|------|------|------|--------|
| POST | `/api/v1/timesheets` | authenticated | `{ id, resourceId, assignmentId, workDate, hours }` | 201 `TimesheetResponse` (`DRAFT`) |
| POST | `/api/v1/timesheets/{id}/submit` | authenticated | — | `TimesheetResponse` (`SUBMITTED`) |
| POST | `/api/v1/timesheets/{id}/approve` | APPROVER/ADMIN | — | `TimesheetResponse` (`APPROVED`) |
| GET | `/api/v1/timesheets?resourceId=&from=&to=` | authenticated | uuid + dates | `TimesheetResponse[]` |

Notes:
- **Offline-first**: the client supplies the `id` (a UUID) on create so offline entries keep their identity. Duplicate id → 409 `TIMESHEET_EXISTS`.
- `hours`: >0 and ≤24.
- Transitions are guarded: submit only from `DRAFT`, approve only from `SUBMITTED` (else 409 `INVALID_TIMESHEET_STATE`).
- Approving a timesheet triggers the project budget's `spentAmount` recalculation.

---

## 10. Notifications — `/api/v1/notifications` · any authenticated user (own only)

`NotificationResponse`: `{ id, userId, type, message, read }`

| Method | Path | Body | Result |
|--------|------|------|--------|
| GET | `/api/v1/notifications` | — | `NotificationResponse[]` (current user's, newest first) |
| POST | `/api/v1/notifications/{id}/read` | — | `NotificationResponse` (`read=true`) |

Notifications are created server-side (e.g. on request approve/reject). Marking another user's notification → 404.

---

## 11. Device tokens (push) — `/api/v1/devices` · any authenticated user

`DeviceTokenResponse`: `{ id, userId, platform }`

| Method | Path | Body | Result |
|--------|------|------|--------|
| POST | `/api/v1/devices/token` | `{ fcmToken, platform }` | 201 `DeviceTokenResponse` |

Registration is idempotent on `fcmToken` (re-registering re-points it to the current user). `platform` ∈ `ANDROID|IOS|WEB`. Push delivery (FCM) is server-config dependent; the API contract is unaffected when push is disabled.

---

## 12. Sync (offline-first) — `/api/v1/sync` · any authenticated user

Generic delta-sync across `USER`, `RESOURCE`, `PROJECT`, `ASSIGNMENT`, `REQUEST`, `TIMESHEET`, `BUDGET` (the `entityType` values). Conflicts are resolved server-side by **last-write-wins on `updatedAt`**.

### POST `/api/v1/sync/push`
Upload a batch of local changes. Each entry carries the row's scalar fields plus the metadata the server needs to detect conflicts.

```jsonc
// request
{
  "changes": [
    {
      "entityType": "RESOURCE",
      "id": "uuid",                       // the row id (client owns it for new rows)
      "payload": { "name": "edited offline", "availabilityStatus": "UNAVAILABLE" },
      "clientUpdatedAt": "2026-08-01T10:00:00Z",  // when the client edited it (drives LWW)
      "clientVersion": 3,                  // version the client based its edit on
      "deleted": false                     // true = soft delete
    }
  ]
}
```
```jsonc
// 200 data (PushResponse)
{
  "appliedCount": 1,
  "conflictCount": 1,
  "conflicts": [
    {
      "entityType": "RESOURCE",
      "id": "uuid",
      "resolution": "CLIENT_WON",          // or "SERVER_WON"
      "message": "Concurrent edit: client (v3, ...) is newer than server (v5, ...) — applied (last-write-wins)"
    }
  ]
}
```

Behaviour per entry:
- `payload` holds **scalar fields only**; server-managed fields (`id`, `createdAt`, `updatedAt`, `version`, `syncStatus`, `deleted`) in the payload are ignored.
- If `clientVersion` matches the server row → straight upsert (no conflict).
- If it differs → **concurrent edit**: last-write-wins by `updatedAt`. The losing side (client or server) is recorded in a server audit log flagged `CONFLICT` and surfaced in `conflicts[]`.
- `deleted: true` performs a soft delete (the row still pulls, with `deleted: true`).
- Applied rows are marked `SYNCED`.
- Unknown `entityType` → 422 `UNSUPPORTED_ENTITY_TYPE`; a payload that violates DB constraints → 422 `SYNC_APPLY_FAILED`.

### GET `/api/v1/sync/pull?since=<ISO-8601>`
Returns every syncable row changed after `since` (**including soft-deletes**), across all entity types, oldest-first.

- `since` is ISO-8601 UTC, e.g. `2026-06-24T08:00:00Z`. Omit (or epoch `1970-01-01T00:00:00Z`) for a full snapshot. Bad format → 422 `INVALID_SINCE`.
- Use the returned `serverTime` as the `since` for your next pull.

```jsonc
// 200 data (PullResponse)
{
  "serverTime": "2026-06-24T12:54:19.425Z",   // pass back as next `since`
  "count": 2,
  "changes": [
    {
      "entityType": "RESOURCE",
      "id": "uuid",
      "payload": { /* full row incl. id, version, updatedAt, deleted, ... */ },
      "updatedAt": "2026-06-24T12:54:07.276Z",
      "version": 2,
      "deleted": false
    },
    {
      "entityType": "RESOURCE",
      "id": "uuid",
      "payload": { /* ... */ },
      "updatedAt": "2026-06-24T12:54:07.282Z",
      "version": 1,
      "deleted": true                          // soft-deleted row still appears in the delta
    }
  ]
}
```

Recommended client loop: keep a `lastSyncedAt` (start at epoch) → `pull?since=lastSyncedAt` → apply rows locally (respecting `deleted`) → set `lastSyncedAt = serverTime` → push local changes → reconcile any `conflicts`.

> Note: a Resource's `skills` collection is **not** part of its sync payload (scalar columns only). Manage skills via `POST /api/v1/resources/{id}/skills` (§5).

---

## 13. Quick start for the UI

```bash
BASE=http://localhost:8080

# 1. Log in (dev seed admin)
curl -X POST $BASE/api/v1/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@cuea.edu","password":"Admin123!"}'
# -> copy data.accessToken

# 2. Call a protected endpoint
curl -H "Authorization: Bearer <ACCESS_TOKEN>" "$BASE/api/v1/resources?page=0&size=20"
```

**Dev seed accounts** (local only):
| Role | Email | Password |
|------|-------|----------|
| ADMIN | `admin@cuea.edu` | `Admin123!` |

(Other users — MANAGER/APPROVER/MEMBER — are created via `POST /api/v1/users` by an ADMIN, or `POST /api/v1/auth/register` for a MEMBER.)

### Interactive docs
When the app is running, live OpenAPI/Swagger UI is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

> Note: endpoints currently lack `@Operation`/`@Schema` annotations, so Swagger shows generic schemas. This Markdown is the authoritative contract until those are added.

---

## 14. Typical end-to-end flow (for wiring screens)

1. **Login** → store `accessToken` + `refreshToken`; refresh when a call returns 401 `UNAUTHENTICATED`.
2. **Admin** sets up: create users, skills, resources (`/users`, `/skills`, `/resources`), add skills to resources.
3. **Manager** creates a project, sets its budget (`/projects`, `/projects/{id}/budget`), and either assigns resources directly (`/projects/{id}/assignments`) or waits for requests.
4. **Member** finds candidates (`/resources/match`), raises a request (`/requests`), logs time (`/timesheets`).
5. **Approver** approves/rejects requests (`/requests/{id}/approve|reject`) and timesheets (`/timesheets/{id}/approve`) → notifications fire, budget spend updates.
6. **Any user** lists notifications and registers a device token for push.
7. **Offline clients** reconcile via `/sync/pull` then `/sync/push` (§12), persisting `serverTime` between runs.
