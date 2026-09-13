# ss-modulith - Study Shield Modulith

## Overview
Single deployable Spring Boot application containing all business modules. Replaces the previous 5-microservice architecture for cost optimization on Render.com free tier.

## Architecture
- **Pattern**: Spring Modulith (modular monolith)
- **Port**: 8080 (single JVM)
- **Database**: Single PostgreSQL, schemas per environment
- **Schema Management**: boot-time `sql/schema.sql` (idempotent) + Hibernate `ddl-auto: update`

## Modules

### common/ (Shared)
- `ResourceNotFoundException` - 404 error handling
- `GlobalExceptionHandler` - Unified error responses
- `DatabaseHealthIndicator` - JDBC health check
- `CorsConfig` - CORS configuration (allowedOriginPatterns: *)
- `RequestLoggingInterceptor` - HTTP request logging
- `WebConfig` - Registers interceptors

### content/ (Content Module)
- **Entities**: ClassLevel, Country, Board, BoardClass, BoardClassSubject (offering), Subject (global), ContentPack, Quiz, Question, QuizBundle
- **Controllers**: ClassLevelController, BoardController, BoardClassController, OfferingController, SubjectController, ContentPackController, QuizController, QuestionController, QuizBundleController
- **API Paths**: `/api/v1/class-levels/**`, `/api/v1/boards/**`, `/api/v1/board-classes/**`, `/api/v1/offerings/**`, `/api/v1/subjects/**`, `/api/v1/content-packs/**`, `/api/v1/quizzes/**`, `/api/v1/questions/**`, `/api/v1/quiz-bundles/**`
- **Seeding**: `AcademicStructureSeeder` (@Order 1, gated `app.academic-structure-seeding.enabled`) builds the global matrix; `AcademicCatalogResolver` resolves display text → ordinals → offerings; `CatalogStartupSeeder` + `QuestionBankContent` fill freemium content.
- **Dependencies**: None (standalone module)

### user/ (User Module)
- **Entities**: User, ParentProfile, ChildProfile
- **Controllers**: UserController, AuthController, ParentController, ParentProfileController, StudentController, ChildProfileController
- **Security**: JwtProvider, JwtAuthFilter, SecurityConfig
- **API Paths**: `/api/auth/**`, `/api/v1/users/**`, `/api/v1/parents/**`, `/api/v1/students/**`, `/api/parents/**`, `/api/students/**`
- **Dependencies**: content (for board/classgrade references)

### quiz/ (Quiz Attempts Module)
- **Entities**: QuizAttempt, AttemptAnswer
- **Controllers**: QuizAttemptController, AttemptAnswerController
- **API Paths**: `/api/v1/quiz-attempts/**`, `/api/v1/attempt-answers/**`
- **Dependencies**: content, user (for ID references)

### feedback/ (Question Review / Feedback Module)
- **Entities**: QuestionFeedback (`question_feedback` table, unique `(account_id, question_id)`)
- **Enums**: FeedbackVote (`UP`/`DOWN`/`NONE`), DownCategory (`WRONG_ANSWER`/`TYPO`/`OFFENSIVE`/`OTHER`)
- **DTOs**: QuestionFeedbackRequest, QuestionFeedbackResponse
- **Controllers**: QuestionFeedbackController (mounted on `/api/v1/questions` alongside `QuestionController`)
- **API Paths**: `PUT /api/v1/questions/{id}/feedback`, `GET /api/v1/questions/{id}/feedback`
- **Dependencies**: content (validates the question exists via `QuestionRepository`)
- **Behavior**: upserts one feedback row per (user, question); report requires a comment; clearing a vote = send `vote=NONE`. Offline-first on mobile via a pending queue + PUT upsert.


### tv/ (TV Device Module)
- **Entities**: User (tv_users), WifiNetwork, ConnectedTV
- **Controllers**: UserController, WifiNetworkController, ConnectedTVController
- **API Paths**: `/api/v1/wifi-networks/**`, `/api/v1/connected-tvs/**`, `/api/v1/tv-users/**`
- **Dependencies**: None (uses external IDs)

### shared/ (Cross-Module Interfaces)
- `ContentReference` - Interface for content module lookups
- `UserReference` - Interface for user module lookups

## Database Schema

All tables live in **per-environment schemas** in one Postgres database:

| Schema | Environment | Active via |
|--------|-------------|------------|
| `"ss-dev"` | development | default profile |
| `"ss-prod"` | production | `prod` profile (`SPRING_PROFILES_ACTIVE=prod`) |

- `hibernate.default_schema` pins the schema (`'"ss-dev"'` by default, `'"ss-prod"'` under the `prod` profile); every Hibernate table resolves there, so no entity pins a table schema.
- `sql/schema.sql` runs on every startup (`spring.sql.init.mode: always`) before `ddl-auto: update`. It is profile-independent: it creates **both** schemas and idempotently backfills shared columns in both, keeping dev and prod in lock-step no matter which profile starts first.
- When prod moves to its own database later, just point `DATABASE_URL` at it from the `prod` profile; nothing else changes.

### Tables (identical in `"ss-dev"` and `"ss-prod"`)
- `class_levels` - Global age-band spine (ordinal 1–17, `slug`/`ordinal` unique, age bounds, stage)
- `countries` - Countries referenced by boards
- `boards` - Board definitions (`country_id`, `min_ordinal`/`max_ordinal` cover)
- `board_class` - Board-local display label per level; UNIQUE `(board_id, class_level_id)`
- `board_class_subject` - Offerings (`offering_id`): UNIQUE `(board_class_id, subject_id)`
- `subjects` - Global subjects (`code` UNIQUE)
- `content_packs` - Content packages per offering (`offering_id`)
- `quizzes` - Quiz definitions
- `questions` - Quiz questions with JSON options, `superseded_by_id` for versioning, `version_group_id`/`version_number` backfilled by schema.sql
- `quiz_bundles` - Idempotent quiz bundle downloads (offerings anchored; `idempotency_key` contains offering ids only, never display names)
- `question_feedback` - one row per (account_id, question_id); vote, down_category, reported, comment, timestamps
- `users` - User accounts (email, password, role, version for optimistic locking, `user_type` backfilled by schema.sql)
- `parent_profiles` - Parent profile details
- `child_profiles` - Child/student profiles (plain `board_id`/`class_grade_id` Long refs by design)
- `quiz_attempts` - Quiz session tracking (version for optimistic locking)
- `attempt_answers` - Individual answer records
- `quiz_results` - Quiz result records
- `tv_users` - TV device users (external reference)
- `wifi_networks` - WiFi network records
- `connected_tvs` - Connected TV devices

## Schema Management
Flyway was removed (commit `30c6fed`). Schema is managed by:
1. `sql/schema.sql` — creates both schemas + idempotently backfills `users.user_type`, `questions.version_group_id`/`version_number`, and pre-adds `boards.min_ordinal`/`max_ordinal`; on legacy databases it also migrates the academic catalog (wipes the content chain, drops `class_grades`, drops `subjects.class_grade_id` and `content_packs.subject_id`) before `ddl-auto` runs. Runs before Hibernate so regular `ADD COLUMN` via `ddl-auto` never hits populated tables.
2. Hibernate `ddl-auto: update` — applies entity changes to the active profile's schema.

## Academic Catalog (matrix)
Content is anchored to **offerings** (`board_class_subject` = board + class ordinal + global subject), never to display names. The `class_levels` ordinal is the single source of truth for age banding; board labels are presentation.

- **Full reference**: see [`ACADEMIC_STRUCTURE.md`](ACADEMIC_STRUCTURE.md) — the matrix, seed coverage, and how to add a board.
- New boards = insert `boards` + `board_class` rows (ordinals 1–16 cover). `AcademicStructureSeeder` ships ALL, CBSE, MH, ICSE, ENG, US, IB from code; gated by `app.academic-structure-seeding.enabled` (default true).

## Question Bank
- **Full process & rules**: see [`QUESTION_BANK_GUIDE.md`](QUESTION_BANK_GUIDE.md) — structure, authorship rules, difficulty ramp, and how to author/load content for new subjects or grades.
- `content/seed/QuestionBankContent.java` — curated source of truth bank. Bands: **Nursery** (age 3), **Junior KG/LKG** (4), **Sr KG/UKG** (5), **Class 1–10** (6–15). Every band has four subjects: **Math, English, EVS, Hindi**. Content ramps from counting/shapes for juniors to board-level (algebra, trigonometry, electricity, civics) for upper classes; follows the common CBSE/ICSE core on the board-agnostic `ALL` board.
- **The backend starts empty**: content seeding may be disabled (`app.catalog-seeding.enabled: false`). Content is loaded on demand via **`POST /api/v1/questions/load`** (`QuestionBankLoader`), which auto-creates Board → BoardClass → offering → ContentPack → Quiz → Question from items carrying `boardCode`/`className`/`age`/`subject`.
- `QuizBundleSeeder` fills freemium quizzes lazily from the curated bank; any class without a curated band is topped up from a real **fallback bank** so a session never starts empty.
- **Subject ordering drives listing, not a cap**: `QuizBundleService` issues **one freemium quiz per active subject that has questions**, ordered by `Subject.displayOrder` (then id). There is no 2-quiz-per-class cap. `SubjectDisplayOrderInitializer` assigns curated defaults (Math=1, EVS=2, English=3, Hindi=4, General Knowledge=5, others=10) idempotently at startup (only where displayOrder is still 0). Rebuild issued bundles with `POST /api/v1/quiz-bundles/rebuild-catalog` (ADMIN) or `FREEMIUM_REBUILD_ON_STARTUP=true`.
- Delivery packs follow the **freemium naming convention** (`Freemium <Subject>`). `QuestionBankLoader` resolves packs to that convention (reusing an existing freemium pack, converting an auto-created `Loaded <Subject>` pack in place, or creating a fresh one), and `QuizBundleService`/`QuizBundleSeeder` select the freemium-named active pack — falling back to any other active pack — so bank-loaded quizzes are always served by the bundle, never invisible.
- Filtering: bundles are per class via **offerings** (board class → global subjects → content packs → quizzes); `QuizBundleResolver`/`AcademicCatalogResolver` fall back to the generic ALL board when a board has no offering at that ordinal. If a bundle request omits `className` and supplies `age`, the class band is derived from the child's age.
- A freemium quiz needs ≥ 3 active questions (`QuizBundleService.MIN_ACTIVE_QUESTIONS_PER_QUIZ`) for a session to start; otherwise it fails fast unless `allowPartial=true`.
- Adding questions later: append to `QuestionBankContent`, use the content admin APIs (`/api/v1/questions`), or POST via the on-demand loader.

## Configuration
- `spring.jpa.hibernate.ddl-auto=update` (Hibernate manages schema; no Flyway)
- `spring.jpa.properties.hibernate.default_schema` = `"ss-dev"` (default profile) or `"ss-prod"` (`prod` profile)
- `spring.sql.init.mode=always` runs `sql/schema.sql` (creates both schemas + backfills)
- Environment selection: default (dev, schema `"ss-dev"`) — run with `SPRING_PROFILES_ACTIVE=prod --server.port=8082` to launch the prod instance (schema `"ss-prod"`)
- `spring.datasource.hikari.maximum-pool-size=2`
- `spring.security.jwt.secret=${JWT_SECRET}`
- `app.jwt.expiration-ms=86400000` (24 hours)

## Authentication & Errors
- `JwtAuthFilter` (OncePerRequestFilter) validates the `Authorization: Bearer` token and builds the `SecurityContext` principal (`ROLE_<role>`).
- A rejected token is recorded on the request (`studyshield.auth_error`) so the client can tell "no credentials" from "invalid/expired token".
- `RestAuthenticationEntryPoint` returns a JSON **401 Unauthorized** for unauthenticated access to protected endpoints — previously Spring's default anonymous **403** was indistinguishable from a real authorization denial, and mobile clients could not detect an expired token to re-login.
- The 401 body: `{"timestamp", "status": 401, "error": "UNAUTHORIZED", "message": "Authentication required" | "Invalid or expired token"}`.

## Deployment
- **Dockerfile**: Multi-stage build (eclipse-temurin:21-jdk-jammy → eclipse-temurin:21-jre-jammy)
- **Health Check**: `/actuator/health`
- **Render.com**: Single Docker Web Service, ~150 hours/month (free tier)
