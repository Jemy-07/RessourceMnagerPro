# Resource Manager Pro — Backend API Reference

Extracted directly from the Spring Boot source (`api/ResourceManagerPro`, package
`com.cuea.rmp`, hexagonal/clean architecture, Java 17, Spring Boot 3.3.5). Every
field name/type below is taken verbatim from the Java DTOs (`record`s), the Flyway
migrations, and the validation annotations — nothing is paraphrased.

**Where the backend has no equivalent feature, this doc says so explicitly** rather
than inventing one — several things the mobile spec implies (delta sync, batch
upload, reports/exports, real-time channel, rate limiting) **do not exist** in this
codebase today. See [Gaps & Inconsistencies](#gaps--inconsistencies-read-this-first).

---

## 0. Conventions that apply to every endpoint

- **Base path**: no `server.servlet.context-path` is configured, so the base URL is
  just `http://<host>:8080/api/v1/...`. Versioning is a literal `/api/v1` prefix
  baked into each `@RequestMapping` — there is no version-negotiation mechanism.
- **Port**: `server.port: 8080` (`application.yml`).
- **Response envelope** — *every* endpoint (success or failure) returns this exact
  shape (`ApiResponse<T>`, `shared/application/ApiResponse.java`):
  ```json
  {
    "success": true,
    "message": "Optional human-readable message, null on plain success",
    "data": { /* T, or null on failure / void */ },
    "errorCode": null,
    "timestamp": "2026-06-24T10:15:30.123Z"
  }
  ```
  `@JsonInclude(NON_NULL)` — null fields (`message`, `errorCode`) are **omitted**
  from the JSON, not sent as `null`. The Android JSON adapter must treat them as
  optional/nullable, not required.
- **Error shape** — identical envelope, `success: false`, `data: null`,
  `errorCode` set to a stable machine code, `message` human-readable:
  ```json
  { "success": false, "message": "Invalid email or password", "data": null, "errorCode": "INVALID_CREDENTIALS", "timestamp": "..." }
  ```
  Validation errors (`@Valid` failures) use the same envelope but `data` is a
  `Map<String,String>` of `fieldName -> message` instead of `null`, and
  `errorCode` is always `"VALIDATION_ERROR"`. This is the **one inconsistency** in
  the error shape — `data` is `Map<String,String>` on validation failures vs.
  always `null` on every other error type.
- **Status code mapping** (`GlobalExceptionHandler`, exhaustive — this is the
  entire exception→status table in the codebase):

  | Exception | HTTP status | errorCode (representative) |
  |---|---|---|
  | `NotFoundException` | 404 | `NOT_FOUND` (or a specific code passed by the throw site) |
  | `ConflictException` | 409 | `CONFLICT`, `EMAIL_ALREADY_EXISTS`, `SKILL_ALREADY_ASSIGNED`, `ASSIGNMENT_CONFLICT`, `REQUEST_ALREADY_DECIDED`, `RESOURCE_UNAVAILABLE`, `TIMESHEET_EXISTS`, `INVALID_TIMESHEET_STATE` |
  | `BusinessRuleException` | **422** Unprocessable Entity | `BUSINESS_RULE_VIOLATION`, `INVALID_DATE_RANGE`, `CURRENCY_MISMATCH`, `INVALID_PROFICIENCY`, `INVALID_HOURS`, `INVALID_ALLOCATION`, `BUDGET_OVER_ALLOCATED`, `INVALID_EMAIL`, `INVALID_RESOURCE`, `INVALID_PROJECT`, `INVALID_REQUEST`, `INVALID_USER` |
  | `UnauthorizedException` | 401 | `UNAUTHORIZED`, `INVALID_CREDENTIALS`, `ACCOUNT_INACTIVE`, `INVALID_REFRESH_TOKEN`, `INVALID_TOKEN`, `UNAUTHENTICATED` |
  | any other `DomainException` subtype | 400 | (fallback — none currently exist beyond the four above) |
  | `MethodArgumentNotValidException` (`@Valid` failure) | 400 | `VALIDATION_ERROR` |
  | Spring Security `AuthenticationException` (no/invalid JWT) | 401 | `UNAUTHENTICATED` (via `RestAuthenticationEntryPoint`) |
  | Spring Security `AccessDeniedException` (role check failed) | 403 | `FORBIDDEN` (via `RestAccessDeniedHandler`) |

  201 Created is returned by every `create`-style POST (register, create user/
  resource/skill/project/assignment/request, log time, register device token).
  Everything else that succeeds returns 200. **There is no 500 handler** — an
  unhandled exception falls through to Spring Boot's default error page/JSON,
  which does **not** use the `ApiResponse` envelope.

- **Pagination** — classic offset style, used only by `list` endpoints for Users,
  Resources, and Projects: query params `page` (0-indexed, default `0`) and `size`
  (default `20`), returning a `PageResult<T>`:
  ```json
  { "content": [ /* T[] */ ], "page": 0, "size": 20, "totalElements": 137, "totalPages": 7 }
  ```
  No cursor pagination anywhere. **Skills, Requests, Notifications, and
  Assignments-by-project are unpaged** — they return a plain JSON array of every
  matching row. There is no sorting or filtering query param on any list endpoint
  except `Requests` (`?status=`, see below).
- **Auth header**: `Authorization: Bearer <accessToken>` on every endpoint except
  `/api/v1/auth/**` and actuator health/info.
- **Soft delete**: every table has a `deleted BIT` column; all repository reads
  filter `deleted = false`. DELETE endpoints (Resource, Project) are soft-deletes,
  not hard deletes — deleted rows are invisible to the API but remain in the DB.
- **Optimistic locking**: every table has a `version BIGINT` column
  (`@Version` on `BaseJpaEntity`), but **no API surfaces `version`** in any
  response DTO — the mobile client cannot see or send it. JPA throws
  `OptimisticLockException` on a stale write internally, but that exception is
  **not mapped** by `GlobalExceptionHandler`, so a concurrent-edit conflict
  currently surfaces as an unhandled 500, not a clean 409.

---

## 1. Authentication & Authorization

### Mechanism
- **Stateless JWT**, HMAC-SHA, via `io.jsonwebtoken` (jjwt 0.12.6).
- Secret: `app.jwt.secret` (base64), default in `application.yml` is a
  **placeholder dev secret** (`ZGV2LW9ubHktc2VjcmV0LWNoYW5nZS1tZS1pbi1wcm9kLTMyLWJ5dGVzLW1pbg==`)
  — must be overridden via `APP_JWT_SECRET` env var in any real deployment.
- **Access token TTL**: 15 minutes (`app.jwt.access-token-ttl-minutes`).
- **Refresh token TTL**: 14 days (`app.jwt.refresh-token-ttl-days`).
- Refresh tokens are tracked server-side in `RefreshTokenStore` (currently an
  **in-memory** `ConcurrentHashMap` impl, `InMemoryRefreshTokenStore` — tokens are
  **lost on server restart** and **not shared across instances**; flagged in the
  source as needing a Redis/DB swap before horizontal scaling).
- **Token rotation**: refresh always revokes the presented refresh token and
  issues a brand-new access+refresh pair. The mobile client must always persist
  the *new* refresh token returned from `/refresh`, never reuse the old one.

### JWT claims
Access token:
```json
{ "sub": "<userId UUID>", "type": "access", "role": "ADMIN|MANAGER|APPROVER|MEMBER", "orgId": "<UUID>", "email": "user@example.com", "iat": ..., "exp": ... }
```
Refresh token:
```json
{ "sub": "<userId UUID>", "jti": "<refresh token id, UUID>", "type": "refresh", "iat": ..., "exp": ... }
```
Note the `type` claim distinguishes access vs refresh — presenting a refresh
token as a Bearer access token (or vice versa) is rejected with
`INVALID_TOKEN`/401.

### RBAC roles (`user.domain.Role`)
Exactly four roles, flat (no hierarchy beyond what's hand-coded into each
`@PreAuthorize`):
```
ADMIN, MANAGER, APPROVER, MEMBER
```
Enforcement is **method-level** `@PreAuthorize` per controller — there is no
central permission matrix file. Full matrix as written in the code:

| Endpoint | Allowed roles |
|---|---|
| `/api/v1/auth/**` | public (no auth) |
| `POST/PUT/DELETE /api/v1/users/**` | `ADMIN` only (class-level `@PreAuthorize`) |
| `POST/PUT/DELETE /api/v1/resources/**`, `POST .../skills` | `ADMIN`, `MANAGER` |
| `GET /api/v1/resources/**`, `/skills`, `/match`, `/availability` | any authenticated user |
| `POST /api/v1/skills` | `ADMIN`, `MANAGER` |
| `POST/PUT/DELETE /api/v1/projects/**`, `POST .../assignments` | `ADMIN`, `MANAGER` |
| `GET /api/v1/projects/**` | any authenticated user |
| `PUT /api/v1/assignments/{id}` | `ADMIN`, `MANAGER` |
| `GET /api/v1/assignments/{id}` | any authenticated user |
| `POST /api/v1/requests` (create) | any authenticated user |
| `GET /api/v1/requests` | any authenticated user |
| `POST /api/v1/requests/{id}/approve|reject` | `ADMIN`, `APPROVER` |
| `POST /api/v1/timesheets` (log), `.../submit` | any authenticated user (no ownership check — see Gaps) |
| `POST /api/v1/timesheets/{id}/approve` | `ADMIN`, `APPROVER` |
| `GET /api/v1/timesheets` | any authenticated user |
| `GET/PUT /api/v1/projects/{id}/budget` | `ADMIN`, `MANAGER` (class-level) |
| `/api/v1/notifications/**`, `/api/v1/devices/**` | any authenticated user (scoped to caller's own `userId`) |

### Endpoints

#### `POST /api/v1/auth/register`
Self-registration. **Always creates a `MEMBER`** — cannot be used to create
ADMIN/MANAGER/APPROVER accounts (those require an existing ADMIN to call
`POST /api/v1/users`).

Request (`RegisterRequest`):
```json
{
  "orgId": "uuid (required)",
  "fullName": "string, required, max 255",
  "email": "string, required, valid email format",
  "password": "string, required, 8-100 chars"
}
```
Response `201`, `data` = `RegisteredUserResponse`:
```json
{ "userId": "uuid", "email": "jane@org.com", "role": "MEMBER" }
```

#### `POST /api/v1/auth/login`
Request (`LoginRequest`):
```json
{ "email": "string, required, valid email", "password": "string, required" }
```
Response `200`, `data` = `AuthResponse`:
```json
{ "accessToken": "<jwt>", "refreshToken": "<jwt>", "tokenType": "Bearer", "expiresIn": 900 }
```
`expiresIn` is seconds (900 = 15 min). Errors: `INVALID_CREDENTIALS` (401) for
bad email/password (deliberately the *same* message+code for both, to avoid
leaking which part was wrong), `ACCOUNT_INACTIVE` (401) if the user was
deactivated.

#### `POST /api/v1/auth/refresh`
Request: `{ "refreshToken": "string, required" }` → Response `200`, same
`AuthResponse` shape as login, with a **rotated** token pair. Errors:
`INVALID_REFRESH_TOKEN` (401) if the token is unknown/revoked/expired,
`ACCOUNT_INACTIVE` (401) if the user was deactivated since the token was issued.

#### `POST /api/v1/auth/logout`
Request: `{ "refreshToken": "string, required" }` → Response `200`,
`data: null`. **Idempotent and best-effort** — an already-invalid token still
returns success (logout never fails). Revokes only the *presented* refresh
token; other devices' sessions for the same user are unaffected (there's no
"log out everywhere" endpoint, though `RefreshTokenStore.revokeAll` exists
internally and is unused by any controller).

---

## 2. Users (`/api/v1/users`) — ADMIN only

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/users` | Create a user (any role) |
| GET | `/api/v1/users/{id}` | Get one user |
| GET | `/api/v1/users?page=&size=` | Paginated list |
| PUT | `/api/v1/users/{id}` | Rename / change role |
| POST | `/api/v1/users/{id}/deactivate` | Soft-deactivate (sets `active=false`) |

**`UserResponse`** (returned by all of the above except deactivate):
```json
{ "id": "uuid", "orgId": "uuid", "fullName": "string", "email": "string", "role": "ADMIN|MANAGER|APPROVER|MEMBER", "active": true }
```
Never includes `passwordHash`.

`POST` body (`CreateUserRequest`):
```json
{ "orgId": "uuid, required", "fullName": "string, required, max 255", "email": "string, required, valid email", "password": "string, required, 8-100 chars", "role": "ADMIN|MANAGER|APPROVER|MEMBER, required" }
```
`PUT` body (`UpdateUserRequest`) — **cannot change email or password**, only:
```json
{ "fullName": "string, required, max 255", "role": "ADMIN|MANAGER|APPROVER|MEMBER, required" }
```
Errors: `EMAIL_ALREADY_EXISTS` (409) on create with a duplicate email (uniqueness
checked among non-deleted rows only — a soft-deleted user's email can be reused).
There is no `DELETE /api/v1/users/{id}` — deactivation is the only removal path,
and deactivating twice throws `USER_ALREADY_INACTIVE` (this is actually thrown as
a `BusinessRuleException` → **422**, not 409, despite reading like a conflict).

---

## 3. Resources & Skills (`/api/v1/resources`, `/api/v1/skills`)

### Entities

**Resource** (`resources` table):

| Field | Type | Notes |
|---|---|---|
| id | UUID (CHAR 36) | domain-generated |
| orgId | UUID | required, immutable |
| userId | UUID, **nullable** | optional link to a platform `User` (e.g. for HUMAN resources) |
| name | string, max 255 | required |
| type | enum `ResourceType` | `HUMAN, EQUIPMENT, FACILITY, MATERIAL` |
| hourlyRate | `{ amount: BigDecimal(19,4), currency: CHAR(3) }` | a `Money` value object — amount ≥ 0, currency is a free 3-letter code (not validated against ISO-4217 list) |
| availabilityStatus | enum `AvailabilityStatus` | `AVAILABLE, UNAVAILABLE` |
| skills | `ResourceSkill[]` | embedded child list, see below |
| + audit fields | createdAt, updatedAt, version, syncStatus, deleted | not exposed in API |

**ResourceSkill** (`resource_skills` table, child of Resource):
```json
{ "skillId": "uuid", "proficiency": 1-5 }
```
Proficiency is strictly `1..5` inclusive (`ResourceSkill.MIN/MAX_PROFICIENCY`).
Adding a skill the resource already has throws `SKILL_ALREADY_ASSIGNED` (409) —
**there is no "update proficiency" or "remove skill" endpoint**, only add.

**Skill** (`skills` table, org-scoped catalog):
```json
{ "id": "uuid", "orgId": "uuid", "name": "string" }
```
Uniqueness of `(orgId, name)` is enforced at the service layer (`SKILL_ALREADY_EXISTS`,
409), not by a DB constraint.

**TimeOff** (`time_offs` table) — **domain and persistence exist, but there is
no controller for it anywhere in the codebase.** A resource's time-off can only
be created by calling code internally (none currently does) — it is **not
reachable over HTTP**. Only `APPROVED` time-off blocks availability; `PENDING`
and `REJECTED` do not. If the mobile app needs to manage time-off, this is a
backend gap that needs a new endpoint, not just a client-side assumption.

### Endpoints

| Method | Path | Roles | Purpose |
|---|---|---|---|
| POST | `/api/v1/resources` | ADMIN/MANAGER | Create resource |
| PUT | `/api/v1/resources/{id}` | ADMIN/MANAGER | Update name/rate/availability (full replace, not partial) |
| DELETE | `/api/v1/resources/{id}` | ADMIN/MANAGER | Soft delete |
| POST | `/api/v1/resources/{id}/skills` | ADMIN/MANAGER | Attach a skill+proficiency |
| GET | `/api/v1/resources/{id}` | any | Get one |
| GET | `/api/v1/resources?page=&size=` | any | Paginated list |
| GET | `/api/v1/resources/{id}/availability?from=&to=` | any | Availability check (see §4) |
| GET | `/api/v1/resources/match?skillId=&from=&to=` | any | Skill+availability matching (see §4) |
| POST | `/api/v1/skills` | ADMIN/MANAGER | Create skill |
| GET | `/api/v1/skills` | any | List all skills (unpaged) |

`from`/`to` query params are `LocalDate` in ISO format (`YYYY-MM-DD`), bound via
`@DateTimeFormat(iso = DATE)`.

**`ResourceResponse`**:
```json
{
  "id": "uuid", "orgId": "uuid", "userId": "uuid|null",
  "name": "string", "type": "HUMAN|EQUIPMENT|FACILITY|MATERIAL",
  "hourlyRateAmount": 75.0000, "currency": "USD",
  "availabilityStatus": "AVAILABLE|UNAVAILABLE",
  "skills": [ { "skillId": "uuid", "proficiency": 4 } ]
}
```

`POST /api/v1/resources` body (`CreateResourceRequest`):
```json
{ "orgId": "uuid, required", "userId": "uuid, optional", "name": "string, required, max 255", "type": "enum, required", "hourlyRateAmount": "decimal ≥ 0, required", "currency": "exactly 3 chars, required" }
```
`PUT` body (`UpdateResourceRequest`) — name/rate/currency/status are all
**required** (it's a full replace, no partial PATCH semantics):
```json
{ "name": "string, required, max 255", "hourlyRateAmount": "decimal ≥ 0, required", "currency": "3 chars, required", "availabilityStatus": "AVAILABLE|UNAVAILABLE, required" }
```
`POST .../skills` body (`AddSkillRequest`):
```json
{ "skillId": "uuid, required", "proficiency": "int 1-5" }
```
`POST /api/v1/skills` body:
```json
{ "orgId": "uuid, required", "name": "string, required, max 255" }
```

---

## 4. Availability & Skills-Matching Logic (server-side, exact algorithm)

This is computed entirely server-side; the mobile app should **not** try to
re-derive it locally except possibly as an optimistic offline placeholder, since
it depends on live assignment/time-off data.

### `GET /api/v1/resources/{id}/availability?from=&to=`
Implemented by `AvailabilityChecker.check()` (`resource/domain`). For a given
resource and `[from, to]` inclusive date window, in this exact order:
1. If `resource.availabilityStatus != AVAILABLE` → **blocked**, reason
   `"Resource is marked UNAVAILABLE"`.
2. Else if any `APPROVED` `TimeOff` for that resource overlaps the window →
   blocked, reason `"Overlaps approved time-off"`.
3. Else → **free**.

**Important caveat written directly in the code**: this check does **not**
consider existing *assignment* overlaps at all — only status + time-off. The
docstring explicitly says assignment-level booking conflicts are handled
separately by `ConflictDetector` at booking time (§5), not by this endpoint. So
"available" from this endpoint does NOT guarantee a booking will succeed if the
resource is already over-allocated for that window.

Response (`AvailabilityResponse`):
```json
{ "resourceId": "uuid", "from": "2026-07-01", "to": "2026-07-15", "available": true, "reason": "Available" }
```
`reason` is always populated (even when `available: true`, it's literally the
string `"Available"`).

### `GET /api/v1/resources/match?skillId=&from=&to=`
`MatchResourcesService`: finds all resources holding `skillId` (via a JPA query
joining `resource_skills`), filters to those passing the same
status+time-off-only availability check above (note: **also ignores assignment
overlap**, same caveat as above), then sorts **descending by proficiency** for
that skill. No tie-breaker is applied for resources with equal proficiency (JPA
result order, effectively insertion order).

Response: array of `ResourceMatchResponse`:
```json
[ { "resourceId": "uuid", "name": "string", "type": "HUMAN", "proficiency": 5, "hourlyRateAmount": 80.0, "currency": "USD" } ]
```

---

## 5. Projects & Assignments (`/api/v1/projects`, `/api/v1/assignments`)

### Entities

**Project** (`projects` table):
```json
{ "id": "uuid", "orgId": "uuid", "managerId": "uuid", "name": "string", "description": "string|null, max 1000", "startDate": "date", "endDate": "date", "status": "PLANNED|ACTIVE|ON_HOLD|DONE" }
```
`ProjectStatus` enum values (exhaustive): `PLANNED, ACTIVE, ON_HOLD, DONE`.
`startDate`/`endDate` form a `DateRange` value object — constructing one with
`end < start` throws `INVALID_DATE_RANGE` (422) at the domain layer, so this is
enforced on **every** date-range-taking endpoint (Project, Assignment, Request,
availability check), not just here.

**Assignment** (`assignments` table, child of Project, references a Resource):
```json
{ "id": "uuid", "projectId": "uuid", "resourceId": "uuid", "title": "string", "startDate": "date", "endDate": "date", "allocationPct": 0-100, "status": "TODO|IN_PROGRESS|DONE" }
```
`AssignmentStatus` enum (exhaustive): `TODO, IN_PROGRESS, DONE`. A `DONE`
assignment stops consuming capacity (`Assignment.consumesCapacity()`) — i.e. once
marked DONE, its allocation no longer counts toward the 100% cap for new
overlapping bookings.

### Endpoints

| Method | Path | Roles | Purpose |
|---|---|---|---|
| POST | `/api/v1/projects` | ADMIN/MANAGER | Create project |
| PUT | `/api/v1/projects/{id}` | ADMIN/MANAGER | Update (full replace incl. status) |
| DELETE | `/api/v1/projects/{id}` | ADMIN/MANAGER | Soft delete |
| GET | `/api/v1/projects/{id}` | any | Get one |
| GET | `/api/v1/projects?page=&size=` | any | Paginated list |
| POST | `/api/v1/projects/{id}/assignments` | ADMIN/MANAGER | Assign a resource (conflict-checked) |
| GET | `/api/v1/projects/{id}/assignments` | any | List all assignments for a project (unpaged) |
| GET | `/api/v1/assignments/{id}` | any | Get one assignment |
| PUT | `/api/v1/assignments/{id}` | ADMIN/MANAGER | Reschedule/reallocate/change status (conflict-checked, excludes itself) |

`POST /api/v1/projects` body (`CreateProjectRequest`):
```json
{ "orgId": "uuid, required", "managerId": "uuid, required", "name": "string, required, max 255", "description": "string, optional, max 1000", "startDate": "date, required", "endDate": "date, required" }
```
`PUT /api/v1/projects/{id}` body — all fields required including `status`:
```json
{ "name": "...", "description": "...", "startDate": "...", "endDate": "...", "status": "PLANNED|ACTIVE|ON_HOLD|DONE, required" }
```
`POST .../assignments` body (`AssignResourceRequest`):
```json
{ "resourceId": "uuid, required", "title": "string, required", "startDate": "date, required", "endDate": "date, required", "allocationPct": "int 0-100" }
```
Response `201`, `AssignmentResponse` as shown above.
`PUT /api/v1/assignments/{id}` body (`UpdateAssignmentRequest`) — note
`resourceId`/`projectId`/`title` **cannot be changed**, only the window/
allocation/status:
```json
{ "startDate": "date, required", "endDate": "date, required", "allocationPct": "int 0-100", "status": "TODO|IN_PROGRESS|DONE, optional/nullable" }
```

### Conflict Detection (`ConflictDetector`, exact algorithm)

Run on **every** create-assignment and update-assignment call (also indirectly
on request-approval, since approval creates an assignment — see §6):

1. Run the same status+time-off `AvailabilityChecker` as §4 (status must be
   `AVAILABLE`, no overlapping `APPROVED` time-off) → if blocked, that reason is
   the conflict.
2. **Capacity check**: sum `allocationPct` of all *other* assignments for the
   same resource that (a) `consumesCapacity()` (i.e. status != `DONE`) and (b)
   `DateRange.overlaps()` the requested window. If
   `sum(existing overlapping allocations) + requestedAllocationPct > 100`, it's a
   conflict, with message:
   `"Over-allocation: {X}% already booked + {Y}% requested exceeds 100% for the window"`
3. If neither trips, the booking is allowed.

This is the **entire double-booking/overbooking algorithm** — it is purely
allocation-percentage-based (not hours-based), and `DateRange.overlaps` is an
inclusive-bounds interval overlap test (`!start.isAfter(other.end) &&
!other.start.isAfter(end)`), so single-day touching ranges (`endA == startB`) DO
count as overlapping.

On conflict: `ConflictException("...", "ASSIGNMENT_CONFLICT")` → **409**. On
update, the assignment being edited is excluded from the "other assignments"
sum (so resizing an existing booking doesn't conflict with itself).

---

## 6. Resource Requests — Approval Workflow (`/api/v1/requests`)

### Entity

**Request** (`requests` table):
```json
{
  "id": "uuid", "requesterId": "uuid", "approverId": "uuid|null",
  "resourceId": "uuid", "projectId": "uuid", "title": "string",
  "startDate": "date", "endDate": "date", "allocationPct": 0-100,
  "status": "PENDING|APPROVED|REJECTED",
  "comments": "string|null, max 1000",
  "decidedAt": "instant|null"
}
```
`RequestStatus` enum (exhaustive, simple state machine, no further states):
`PENDING → APPROVED` or `PENDING → REJECTED`. **Both are terminal** — a second
decision on an already-decided request throws `REQUEST_ALREADY_DECIDED` (409).
There is no "re-open"/"cancel" endpoint.

### Endpoints

| Method | Path | Roles | Purpose |
|---|---|---|---|
| POST | `/api/v1/requests` | any authenticated | Raise a request (requesterId = caller, from JWT) |
| GET | `/api/v1/requests?status=` | any authenticated | List all requests, optional status filter |
| POST | `/api/v1/requests/{id}/approve` | ADMIN/APPROVER | Approve → creates the Assignment |
| POST | `/api/v1/requests/{id}/reject` | ADMIN/APPROVER | Reject with required comments |

`status` query param accepts `PENDING|APPROVED|REJECTED`; omitted = all
statuses, no pagination on this endpoint (full unpaged array). **There is no
filter for "my requests" or "requests awaiting my approval"** — the client must
filter the full list locally by `requesterId`/`status`.

`POST /api/v1/requests` body (`CreateRequestRequest`):
```json
{ "resourceId": "uuid, required", "projectId": "uuid, required", "title": "string, required", "startDate": "date, required", "endDate": "date, required", "allocationPct": "int 0-100" }
```
`requesterId` is **not** in the body — it's taken from the authenticated JWT
(`currentUser.currentUserId()`), so the client cannot create a request on
someone else's behalf.

`POST .../approve` — **no body**. `approverId` likewise comes from the JWT.

`POST .../reject` body (`RejectRequestRequest`):
```json
{ "comments": "string, required (non-blank), max 1000" }
```
Note: the web-layer validation makes `comments` **required** on reject (`@NotBlank`)
even though the domain `Request.reject()` method itself accepts a null comment —
the stricter rule is enforced only at the HTTP boundary.

### Workflow logic (exact, from `CreateRequestService` / `ApproveRequestService`)

1. **On create**: runs the §4 availability check (status + approved time-off
   only) up front. If unavailable → `RESOURCE_UNAVAILABLE` (409) and the request
   is never persisted. This means a request *can* still later fail at approval
   time if another booking was approved in the meantime (no allocation/conflict
   check happens at create time — only at approve time).
2. **On approve**: in a single transaction —
   a. `Request.approve(approverId, now)` — flips status, sets `decidedAt`, fails
      with `REQUEST_ALREADY_DECIDED` if not currently `PENDING`.
   b. Calls the **same** `AssignResourceUseCase` as a direct assignment POST
      (§5) — i.e. the full `ConflictDetector` (status + time-off + 100% capacity
      cap) runs again at this point. **If this throws `ASSIGNMENT_CONFLICT`, the
      whole transaction rolls back and the request reverts to `PENDING`** (Spring
      `@Transactional` rollback) — the approve call itself returns the
      ASSIGNMENT_CONFLICT error to the caller; the request is *not* left in a
      half-approved state in the DB.
   c. Sends an `APPROVAL`-type notification to the requester:
      `"Your request '{title}' was APPROVED"`.
3. **On reject**: flips status, stores `comments`, sets `decidedAt`, sends an
   `APPROVAL`-type notification (yes — rejection also uses `NotificationType.APPROVAL`,
   there is no separate `REJECTION` type; see §8) with message
   `"Your request '{title}' was REJECTED: {comments}"`.

**Approver assignment is not role/relationship-scoped** — any user with
`ADMIN` or `APPROVER` role can approve/reject *any* pending request system-wide;
there's no concept of "this project's designated approver."

---

## 7. Timesheets (`/api/v1/timesheets`)

### Entity

**Timesheet** (`timesheets` table):
```json
{ "id": "uuid", "resourceId": "uuid", "assignmentId": "uuid", "workDate": "date", "hours": "decimal(5,2)", "status": "DRAFT|SUBMITTED|APPROVED" }
```
`TimesheetStatus` (exhaustive): `DRAFT → SUBMITTED → APPROVED`, strictly linear,
no rejection/recall state. `hours` must be `> 0` and `≤ 24` (validated both at
the web layer `@Positive @DecimalMax("24.00")` and again in the domain
`Timesheet.requireHours`).

### **Critically for the mobile offline-sync design**: the `id` is client-supplied

`LogTimeCommand`/`LogTimeRequest` take an explicit `id: UUID` field generated by
the **caller**, not the server. This is the only entity in the entire codebase
designed this way, specifically (per the code comments) so offline-created
entries keep a stable identity through a later sync. **However**:
- There is **no upsert/idempotent-retry semantics**. `LogTimeService` calls
  `existsById(id)` first and throws `TIMESHEET_EXISTS` (409 `ConflictException`)
  if the id is already present — a naive "retry the same POST if the network
  call's response was lost" will fail with 409, not succeed silently. The mobile
  sync layer must treat `TIMESHEET_EXISTS` as "already synced, treat as success"
  rather than a real error.
- There is **no batch/bulk endpoint** — every offline timesheet entry must be
  POSTed individually. For a WorkManager-driven sync of many queued entries,
  expect N sequential (or parallel) `POST /api/v1/timesheets` calls, each
  individually idempotent-by-409 as above.

### Endpoints

| Method | Path | Roles | Purpose |
|---|---|---|---|
| POST | `/api/v1/timesheets` | any authenticated | Log a DRAFT entry (client-supplied id) |
| POST | `/api/v1/timesheets/{id}/submit` | any authenticated | DRAFT → SUBMITTED |
| POST | `/api/v1/timesheets/{id}/approve` | ADMIN/APPROVER | SUBMITTED → APPROVED, triggers budget recalc |
| GET | `/api/v1/timesheets?resourceId=&from=&to=` | any authenticated | List entries in a date range for one resource |

**No ownership check** is enforced in the service layer for log/submit — any
authenticated user can log time for *any* `resourceId`, not just their own
linked resource. (`@PreAuthorize("isAuthenticated()")` only — no
`resourceId == currentUser`'s-linked-resource check.) Treat this as a current
backend gap if you need per-user write scoping; don't assume the server enforces
"can only log my own hours."

`POST /api/v1/timesheets` body (`LogTimeRequest`):
```json
{ "id": "uuid, required (client-generated)", "resourceId": "uuid, required", "assignmentId": "uuid, required", "workDate": "date, required", "hours": "decimal, required, >0 and ≤24" }
```
`submit`/`approve` — path id only, no body.
`GET` list — `resourceId`, `from`, `to` are all **required** query params (no
default "current user" — caller must pass `resourceId` explicitly).

State-transition errors: `INVALID_TIMESHEET_STATE` (409) if you `submit` a
non-DRAFT or `approve` a non-SUBMITTED timesheet.

### Budget side-effect on approval
`ApproveTimesheetService` calls `BudgetRecalculationPort.recalculateForAssignment`
after flipping status — see §8 for the exact formula. This is a **synchronous**
side effect inside the same HTTP request/transaction, not an async job.

---

## 8. Budgets (`/api/v1/projects/{projectId}/budget`)

ADMIN/MANAGER only (class-level `@PreAuthorize`). One-to-one with a Project
(`UNIQUE` constraint on `budgets.project_id`).

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/v1/projects/{projectId}/budget` | Get the project's budget |
| PUT | `/api/v1/projects/{projectId}/budget` | Set total/allocated (creates the budget row if absent) |

**There is no `POST /api/v1/budgets` or list-all-budgets endpoint** — a budget
only exists scoped under its project, and can only be fetched one project at a
time.

`BudgetResponse`:
```json
{
  "id": "uuid", "projectId": "uuid", "currency": "USD",
  "totalAmount": 100000.0000, "allocatedAmount": 80000.0000, "spentAmount": 23500.0000,
  "margin": 76500.0000, "remaining": 20000.0000
}
```

### Formulas (exact, `Budget` domain class)
- `margin = totalAmount − spentAmount`
- `remaining = totalAmount − allocatedAmount`
- **`spentAmount` is never client-settable** — it's recomputed *only* by
  `RecalculateSpendService`, triggered *only* by timesheet approval (§7). The
  formula:
  ```
  spend = Σ over every Assignment in the project:
            Σ over every APPROVED Timesheet for that assignment:
              timesheet.hours × resource.hourlyRate.amount
  ```
  **No FX conversion** — the code comment explicitly assumes every resource's
  hourly rate shares the budget's currency; mixing currencies across resources
  on the same project will silently produce a wrong number (no validation error
  is thrown for a currency mismatch in this specific path — `Money.of` is just
  called with the budget's currency unconditionally).
- `allocatedAmount` is settable directly via `PUT`, and must not exceed
  `totalAmount` (`BUDGET_OVER_ALLOCATED`, 422) — this is the only validation
  rule on allocation; there's no "allocated must cover all assignment costs"
  check.

`PUT` body (`AllocateBudgetRequest`):
```json
{ "totalAmount": "decimal ≥ 0, required", "allocatedAmount": "decimal ≥ 0, required", "currency": "exactly 3 chars, required" }
```
If you `PUT` with a different `currency` than the existing budget, `Budget.allocate`
throws `CURRENCY_MISMATCH` (422) — currency is effectively immutable once set
(short of going through `findByProjectId().orElseGet(create)` with a brand-new
project that has no budget row yet).

**There is no budget-vs-margin alert/threshold system, no per-resource cost
breakdown endpoint, and no historical/time-series budget data** — `GET` always
returns only the current snapshot.

---

## 9. Notifications & Push (`/api/v1/notifications`, `/api/v1/devices`)

### Mechanism: **FCM push, not WebSocket/SSE**
There is no WebSocket or Server-Sent-Events endpoint anywhere in this codebase.
Real-time-ish delivery is exclusively via Firebase Cloud Messaging
(`firebase-admin` 9.4.3), triggered synchronously inside the same request that
caused the event (e.g. inside the `approve`/`reject` request transaction). The
mobile app must integrate the Firebase Android SDK and register its FCM token
via `/api/v1/devices/token`; there is no polling-friendly "long-poll" endpoint
either — `GET /api/v1/notifications` is a plain on-demand fetch.

**FCM is disabled by default** in dev (`app.fcm.service-account-path` blank) —
pushes become a logged no-op server-side; this only matters for backend
configuration, not the mobile client, but means you cannot rely on push
notifications actually arriving against a default local dev backend.

Push delivery is explicitly **fail-soft**: a bad/expired/unregistered FCM token
never throws and never affects whether the underlying notification/business
action succeeds — the mobile client should not expect any error feedback if a
push silently fails to deliver; rely on `GET /api/v1/notifications` as the
source of truth, not push receipt.

### `NotificationType` enum (exhaustive — only 3 values exist)
```
CONFLICT, APPROVAL, DEADLINE
```
**Important**: in the current code, only `APPROVAL` is ever actually emitted —
both request-approve *and* request-reject use `NotificationType.APPROVAL` (no
distinct `REJECTED` type exists). `CONFLICT` and `DEADLINE` are defined in the
enum but **nothing in the codebase currently triggers them** — there is no
automatic deadline-checker job and no conflict-detector hook that calls
`NotificationPort.notify` with those types. Don't assume the backend proactively
warns about upcoming deadlines or overbooking outside of the synchronous
`ASSIGNMENT_CONFLICT` HTTP error at booking time.

### Entities

**Notification** (`notifications` table):
```json
{ "id": "uuid", "userId": "uuid", "type": "CONFLICT|APPROVAL|DEADLINE", "message": "string, max 1000", "read": false }
```

**DeviceToken** (`device_tokens` table):
```json
{ "id": "uuid", "userId": "uuid", "platform": "ANDROID|IOS|WEB" }
```
(`fcmToken` itself is never echoed back in any response, by design.)
`Platform` enum (exhaustive): `ANDROID, IOS, WEB`.

### Endpoints

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/v1/devices/token` | Register/re-point this device's FCM token to the caller |
| GET | `/api/v1/notifications` | List the caller's own notifications (unpaged, all statuses) |
| POST | `/api/v1/notifications/{id}/read` | Mark one of the caller's own notifications read |

`userId` is always the caller's JWT identity — there's no way to register a
token or list notifications for any other user.

`POST /api/v1/devices/token` body (`RegisterDeviceTokenRequest`):
```json
{ "fcmToken": "string, required, max 512", "platform": "ANDROID|IOS|WEB, required" }
```
Registration is **idempotent on the token string** — re-registering the same
`fcmToken` (e.g. after Firebase rotates it for the same install, or a user
re-logs-in on the same device) updates the existing row's `userId`/`platform`
rather than erroring or duplicating. There is **no "unregister/delete token"**
endpoint — uninstalling the app or logging out doesn't clean up the server-side
token row.

`markRead` on a notification that doesn't belong to the caller returns
`NOT_FOUND` (404) — not 403 — by design (`MarkAsReadService` filters by
`userId` before the lookup even resolves, so you can't distinguish "doesn't
exist" from "exists but isn't yours").

There is **no "mark all read"** bulk endpoint, and **no DELETE** for
notifications.

---

## 10. Sync, Offline Support & Idempotency — what's actually there

Read this section carefully — it directly contradicts what a "Sync & Offline
Support" section is normally expected to contain.

- **No delta/incremental sync endpoint exists.** There is no
  `?updatedSince=` / `?since=` parameter, no `/sync` endpoint, and no
  changes-feed anywhere in any controller. Every `sync_status` column
  (`PENDING`/`SYNCED`, see below) exists in the DB schema and on `BaseJpaEntity`,
  but **no repository query or controller ever reads or filters by it** — it is
  written once on insert (defaults to `PENDING` from `BaseJpaEntity`) and never
  updated or exposed afterward. It is effectively dead schema today, not a
  working sync primitive. **Do not design the Android sync layer around polling
  this field via the API** — there's nothing on the server side to compare
  against.
- **No `updatedAt`-based polling is exposed either** — `createdAt`/`updatedAt`
  exist on every JPA entity but are **not included in any of the response DTOs**
  (`UserResponse`, `ResourceResponse`, `ProjectResponse`, etc. all omit them).
  The mobile app cannot ask "give me everything changed since X" because the
  API literally never returns a timestamp to compare against, on any resource.
- **No conflict-resolution endpoint and no client-visible `version`.** Optimistic
  locking exists at the JPA layer (`@Version`) but is invisible over HTTP (see
  §0) — if you build offline-edit-then-sync for, say, Projects or Resources, you
  cannot detect a stale-write conflict via the API today; a `PUT` will just
  silently overwrite server state, or in the rare case of a true concurrent
  write inside the same millisecond, throw an unmapped 500.
- **Idempotency keys**: not a general mechanism. The one place idempotency
  matters (Timesheet log, §7) is handled by **client-supplied UUID + 409 on
  duplicate**, not a dedicated `Idempotency-Key` header. No other POST endpoint
  accepts a client-supplied id or has any duplicate-submission protection —
  e.g. POSTing the same Project-create twice (same body, different network
  retry) will create two distinct projects with two different server-generated
  ids.
- **No batch/bulk endpoints anywhere** — not for timesheets, not for
  assignments, not for anything. Every write is a single-entity call.

### Practical implication for the Android app
Given the above, the realistic sync strategy with *this* backend, as it
currently stands, is:
1. **Pull**: full-list GETs (paginated where available) — there is no way to
   ask for only what changed, so a "sync" pull is really "re-fetch and diff
   locally against Room."
2. **Push**: queue mutations in WorkManager and replay them as ordinary POST/
   PUT/DELETE calls when connectivity returns. Only the Timesheet-log path is
   safe to blindly retry (dedupe via client UUID + treat `TIMESHEET_EXISTS` as
   success); every other mutation needs your own local idempotency layer (e.g.
   a Room-side "already-submitted" flag keyed by a locally generated correlation
   id) since the server provides none.
3. If true multi-device conflict resolution is a hard requirement, this is a
   backend gap to raise with whoever owns this API, not something the mobile
   client can paper over alone — there's no version/ETag to compare.

---

## 11. Reports, Exports, Dashboards

**None of this exists.** A repo-wide check confirms:
- No aggregation/report/dashboard controller or endpoint anywhere
  (utilization, cost rollups, skills-gap, compliance — none implemented).
- No PDF/CSV/Excel export, no file-download endpoint, no `MultipartFile` usage
  anywhere (no file upload exists either).
- No `/api/v1/budgets` list-all (only per-project GET, §8).
- Any "Reports" screen in the mobile app today would have to be computed
  **entirely client-side** from the raw list endpoints (Projects, Assignments,
  Timesheets, Budgets per-project) — there is no shortcut on the backend.

---

## 12. Config / Environment — what the mobile client needs to know

- **Base URL**: `http://<host>:8080/api/v1` in dev (`application.yml` /
  `application-dev.yml`, active profile defaults to `dev`). No separate
  staging/prod URLs are defined anywhere in this repo — those would be
  externally configured (env vars / infra), not discoverable from source.
- **No `server.servlet.context-path`** — don't prefix with anything beyond
  `/api/v1`.
- **CORS**: **no `CorsConfiguration`/`@CrossOrigin` exists anywhere** in the
  codebase. This is irrelevant for a native Android HTTP client (CORS is
  browser-enforced), but flag it if a companion web client is ever planned —
  it will need this added.
- **Rate limiting**: **none configured** — no Bucket4j/resilience4j dependency,
  no filter. Don't expect 429s; the server will not throttle you (and won't
  protect itself if you hammer it).
- **Request size limits**: only Spring Boot's untouched defaults (no
  `server.tomcat.max-http-form-post-size` or `spring.servlet.multipart.*`
  overrides in either yml file) — and moot anyway since there's no file upload.
- **Health/info**: `GET /actuator/health` and `/actuator/info` are public
  (no auth) — useful for an Android-side "is the server reachable" check before
  attempting login. `show-details` is `when_authorized` in the base profile and
  `always` in dev — don't rely on detailed health info being visible without
  auth in anything other than local dev.
- **API docs**: springdoc-openapi is wired in (`/v3/api-docs/**`,
  `/swagger-ui/**`, `/swagger-ui.html` — all public, no auth). **This is the
  single best way to double-check this document against the live server** —
  hit `/v3/api-docs` for the generated OpenAPI 3 JSON schema if anything here
  ever drifts from a running instance.
- **DB**: MariaDB via Flyway migrations (`V1`..`V7`), `ddl-auto: validate` (no
  Hibernate auto-schema-generation in any environment) — irrelevant to the
  mobile client directly, but explains why every column nullability/type above
  is authoritative (the JPA entities are validated against, not generators of,
  the schema).

---

## Gaps & Inconsistencies (read this first)

A consolidated list of every place this codebase deviates from "typical" REST/
mobile-backend expectations, gathered while reading the source above — treat
each as either a backend ticket to raise, or a constraint to design around:

1. **No delta sync, no `updatedAt` in any response, no usable `version`/ETag.**
   The `sync_status` column exists on every table but is dead — never read,
   never updated past its initial `PENDING` default. (§10)
2. **No batch/bulk endpoints anywhere** — every mutation is one row per HTTP
   call. (§7, §10)
3. **Timesheet log is idempotent-by-convention (client UUID + 409-as-success)**;
   *no other endpoint* has any duplicate-submission protection — retries of
   Project/Resource/Assignment/Request creation will create duplicates. (§10)
4. **No reports/exports/dashboards/aggregation endpoints at all.** (§11)
5. **No WebSocket/SSE** — push is FCM-only, and disabled by default in dev.
   `CONFLICT` and `DEADLINE` notification types are defined but never emitted by
   any code path today — only `APPROVAL` (used for both approve *and* reject).
   (§9)
6. **TimeOff has full domain/persistence support but zero HTTP exposure** — no
   controller exists to create/list/approve/reject time-off. (§3)
7. **No ownership scoping on Timesheet log/submit** — any authenticated user can
   log hours against any `resourceId`. (§7)
8. **Optimistic-lock conflicts (`@Version`) are not mapped by
   `GlobalExceptionHandler`** — a genuine concurrent-write race surfaces as an
   unhandled 500, not a clean 409, and is invisible to the client beforehand
   since `version` is never returned in any response DTO. (§0)
9. **Validation-error responses use a different `data` shape** (`Map<String,String>`)
   than every other response (`data: null` or the actual resource) — same
   envelope, different payload type under failure. (§0)
10. **Update endpoints are full-replace, not partial PATCH** — e.g.
    `PUT /resources/{id}` requires name+rate+currency+status every time; there's
    no PATCH anywhere in the API.
11. **No "unregister device token" endpoint** — logout/uninstall doesn't clean
    up FCM registrations server-side. (§9)
12. **Rejecting a Request requires non-blank `comments`** at the web-validation
    layer, but the domain model itself treats `comments` as optional — the
    stricter rule only exists in `RejectRequestRequest`'s `@NotBlank`. (§6)
13. **No staging/prod base URLs defined in source** — only the dev profile is
    checked in; expect those to come from build-time config (Gradle
    buildConfigField / env-specific `BuildConfig`), not from this repository.
