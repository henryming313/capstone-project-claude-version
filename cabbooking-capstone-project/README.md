# 🚖 Cab Booking System

### Capstone Project – Centria University of Applied Sciences

Uber-like ride-hailing MVP built with **Spring Boot 3 + MySQL**, rebuilt from the
original project's README/report specification (role-based access, trip
lifecycle state machine, fixed-table fare estimation, ratings, admin console).

> **Note on this rebuild:** the original three uploaded files (`pom.xml`,
> `README.md`, `Final_Report_v1.docx`) contained the build config, docs and
> report — but no Java source. Everything under `src/` below was written
> from scratch to match that specification. It has **not been compiled
> against a live Maven repository** (this sandbox has no network access to
> Maven Central), so run `mvn clean install` yourself before relying on it,
> and check the "Known limitations" section at the bottom.

---

## Tech stack

| Layer | Choice |
|---|---|
| Backend | Java 17, Spring Boot 3.5.11, Spring Data JPA, Bean Validation |
| Auth | **No JWT / no HTTP session** (MVP choice, matches original project) — see below |
| Password storage | BCrypt hash (`spring-security-crypto`, no full Security filter chain) |
| Database | MySQL 8 |
| API docs | springdoc-openapi → Swagger UI |
| Frontend | Static HTML + vanilla JS + Axios + Leaflet (served by Spring Boot itself) |

## Auth model (why there's no login "session")

The original MVP had no JWT (it's listed in the report as a *future*
improvement) and no server session either. This rebuild keeps that shape:

1. `POST /api/auth/login` verifies the BCrypt hash and returns the user's
   `{id, username, role, status}`.
2. The frontend stores that in `localStorage` and re-sends the relevant id
   (`riderId`, `driverId`, `adminId`) as a query param / body field on every
   later call.
3. Every service method that performs a role-sensitive action calls
   `UserService.requireActiveUserWithRole(id, expectedRole)` first — so
   authorization is still enforced **server-side**, it's just identified by
   an id rather than a signed token. This is intentionally the same trust
   model as the original project, not a production-grade auth scheme.

If you want real auth later, swap `AuthService`/`UserService` calls for a
JWT filter — the layering (controller → service → repository) already
isolates that change to the edges.

---

## Project structure

```
src/main/java/com/centria/cabbooking/
  CabbookingApplication.java
  common/enums/         Role, AccountStatus, CabStatus, TripStatus (+ state machine)
  entity/               User, Cab, TripBooking, Rating, DriverCabAssignment, TripRejection
  repository/           Spring Data JPA repositories
  dto/request|response/  Request/response DTOs (password hash never leaves the backend)
  service/               AuthService, UserService, CabService, TripService, RatingService,
                          FareService (fixed base + route surcharge table), LocationService
  controller/            Auth, AdminUser, Cab, Trip, Rating, Location
  exception/             Custom exceptions + @RestControllerAdvice global handler

src/main/resources/
  application.properties
  data.sql                seed users/cabs (admin, rider1, driver1) — safe to re-run
  static/frontend/         login/register/rider/driver/admin HTML + css/js (Axios + Leaflet)
```

## Trip lifecycle (enforced in `TripStatus.canTransitionTo`)

```
PENDING → ACCEPTED → IN_PROGRESS → COMPLETED
PENDING → CANCELLED
ACCEPTED → CANCELLED
```

Any other transition (e.g. completing a trip that was never started) is
rejected with HTTP 409 by the backend, regardless of what the frontend
sends — this was a specific defect called out in the original project
report and is why the check lives in the enum itself rather than being
re-implemented per endpoint.

Rejecting a trip does **not** cancel it — it records a `trip_rejections`
row so the trip stays visible to *other* drivers while the rejecting
driver won't be shown it again (`GET /api/trips/available`).

## Fare estimation

Fixed base fare (€5) + a fixed surcharge per route pair, matching the
report's example (Centria University → Kokkola Railway Station = €5 + €8 =
€13). See `FareService` — swap the matrix for a distance/time-based
formula later without touching the trip schema.

---

## Frontend dependencies are vendored locally (no CDN)

`axios` and `leaflet` ship inside `static/frontend/vendor/` and are loaded
via relative `<script src="vendor/...">` tags — not a CDN. This was a
deliberate fix: the first version of this project loaded axios from
`cdnjs.cloudflare.com`, and in network-restricted setups (e.g. WSL without
outbound internet, campus firewalls) that script silently fails to load,
which then surfaces in the browser as **`api is not defined`** on the
login page — `js/api.js` never got to run `axios.create(...)` because
`axios` itself never loaded. If you ever see that error again, open the
browser DevTools console: a `net::ERR_*` or 404 on a `<script>` tag is the
tell. (Google Fonts is still loaded remotely for typography only — if
that's blocked too, the page just falls back to the system font, no
functional impact.)

### If login throws `Zero date value prohibited` / `Could not extract column ... created_at`

This means a row in the database (e.g. the seeded `admin` user) actually
has `created_at = '0000-00-00 00:00:00'` stored — leftover corrupt data
from an earlier failed `ALTER TABLE ... ADD COLUMN created_at ... NOT
NULL` on a non-empty table (see the section above). There is no way to
"repair" that value in place that's worth the effort — **drop and
recreate the database**:

```sql
DROP DATABASE IF EXISTS cabbooking_mvp;
CREATE DATABASE cabbooking_mvp;
```

Then restart the app. Make sure you're pointing at the *same* MySQL
instance/database you just dropped — if you copy the project into a new
folder but leave `application.properties` pointing at the old
`cabbooking_mvp` database, the corrupt rows are still there and the error
persists. The datasource URL also now appends
`zeroDateTimeBehavior=CONVERT_TO_NULL` as a safety net so any leftover
zero-dates convert to `null` instead of throwing a 500, but a clean
database is the real fix.

### If you saw `Could not initialize proxy [...User#2] - no session`

This is a classic Hibernate "lazy-loading outside a transaction" error. A
few read-only service methods (`TripService.listForRider`,
`listAvailableForDriver`, `getDriverEarnings`, `RatingService.
listForDriver`, etc.) were missing `@Transactional`. Since `TripBooking`'s
`rider`/`driver`/`cab` associations are `FetchType.LAZY` and
`spring.jpa.open-in-view=false`, the repository call's own transaction
closed the Hibernate session right after fetching the `TripBooking` rows
— then mapping them to DTOs (which reads `trip.getRider().getUsername()`
etc.) tried to lazily initialize those associations with no session left
to do it in. Fixed by adding `@Transactional(readOnly = true)` to every
read method that walks a lazy association. If you add new read methods
that touch `.getRider()`/`.getDriver()`/`.getCab()` on a `TripBooking`
later, remember to annotate them the same way (or switch the repository
query to `JOIN FETCH` the association eagerly).

## Getting started

### If you saw `Data truncated for column 'status'` / `0000-00-00` warnings on startup

This means MySQL already had tables from an earlier run of the app (with
rows in them), and `ddl-auto=update` tried to `ALTER` those existing,
non-empty tables into the current shape — MySQL's native `ENUM` type and
`NOT NULL` columns without defaults are both fragile to alter once rows
exist. These are logged as `WARN`, not fatal — check for `Started
CabbookingApplication in ...` further down the log; if it's there, the
app is actually running fine despite the warnings.

Two independent things fix this:

1. **Entities now force `VARCHAR` for all status/role enum columns**
   (`@JdbcTypeCode(SqlTypes.VARCHAR)`) instead of letting Hibernate 6
   default to a native MySQL `ENUM(...)` type, which is what made the
   `ALTER TABLE ... MODIFY COLUMN status enum(...)` migration so brittle
   in the first place.
2. **For a guaranteed-clean slate** (recommended once, now, since your
   tables already have leftover state from earlier runs):
   ```sql
   DROP DATABASE cabbooking_mvp;
   CREATE DATABASE cabbooking_mvp;
   ```
   Then restart the app — Hibernate will `CREATE TABLE` fresh (no `ALTER`
   involved) and `data.sql` reseeds `admin` / `rider1` / `driver1`.

### 1. Database

```sql
CREATE DATABASE cabbooking_mvp;
```

No manual schema import needed — `spring.jpa.hibernate.ddl-auto=update`
creates all six tables from the JPA entities on first run, and
`data.sql` seeds three test accounts + two cabs (see below).

### 2. Configure `src/main/resources/application.properties`

```properties
spring.datasource.username=root
spring.datasource.password=yourpassword
```

### 3. Run

```bash
mvn clean install
mvn spring-boot:run
```

### 4. Open the app

```
http://localhost:8081/                     → redirects to the login page
http://localhost:8081/swagger-ui.html      → interactive API docs
```

### Test accounts (seeded by `data.sql`)

| Username | Password | Role |
|---|---|---|
| admin | admin123 | ADMIN |
| rider1 | rider123 | RIDER |
| driver1 | driver123 | DRIVER (pre-assigned to cab `KOK-101`) |

A typical demo flow: log in as `rider1` → book a trip → log in as
`driver1` (new tab/incognito) → accept it on the "Available Requests"
panel → start → complete → back in `rider1`'s tab, rate the trip.

---

## Known limitations / things to check before you rely on this

- **Not compiled in this environment.** I wrote and statically reviewed
  every file (brace-matched all `.java` files, syntax-checked all `.js`
  files) but couldn't run `mvn compile` — this sandbox has no egress to
  Maven Central. Please run a build locally first.
- Auth is intentionally the original MVP's id-based model, not JWT —
  don't deploy this to a public network as-is.
- The fare "surcharge matrix" in `FareService` is illustrative, not
  derived from real driving distances — swap it out if you need accurate
  pricing.
- CORS is wide-open (`allowedOriginPatterns("*")`) for local dev
  convenience since the frontend is normally same-origin; tighten it if
  you split the frontend onto a different host.

## Future improvements (from the original report, still open)

* JWT-based authentication
* Google Maps API / real-time GPS tracking
* Online payment integration
* Mobile application

## 👥 Team Members

| Name | Role |
|---|---|
| Mazharul Islam | Project Manager |
| Zheng Minghao | Backend Developer |
| Huang Yuyuan | Full-Stack Developer |
| Peng Miaoyang | QA & UI Developer |

## 📜 License

Developed for academic purposes — Centria UAS Capstone Project.
