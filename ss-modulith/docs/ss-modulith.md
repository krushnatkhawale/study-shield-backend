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
- **Entities**: Board, ClassGrade, Subject, ContentPack, Quiz, Question, QuizBundle
- **Controllers**: BoardController, ClassGradeController, SubjectController, ContentPackController, QuizController, QuestionController, QuizBundleController
- **API Paths**: `/api/v1/boards/**`, `/api/v1/class-grades/**`, `/api/v1/subjects/**`, `/api/v1/content-packs/**`, `/api/v1/quizzes/**`, `/api/v1/questions/**`, `/api/v1/quiz-bundles/**`
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
- `boards` - Board definitions
- `class_grades` - Grade levels per board (name is source of truth, no grade_number)
- `subjects` - Subjects per grade (`display_order` backfilled by schema.sql)
- `content_packs` - Content packages per subject
- `quizzes` - Quiz definitions
- `questions` - Quiz questions with JSON options, `superseded_by_id` for versioning, `version_group_id`/`version_number` backfilled by schema.sql
- `quiz_bundles` - Idempotent quiz bundle downloads (renamed from freemium_packs)
- `question_feedback` - one row per (account_id, question_id); vote, down_category, reported, comment, timestamps
- `users` - User accounts (email, password, role, version for optimistic locking, `user_type` backfilled by schema.sql)
- `parent_profiles` - Parent profile details
- `child_profiles` - Child/student profiles
- `quiz_attempts` - Quiz session tracking (version for optimistic locking)
- `attempt_answers` - Individual answer records
- `quiz_results` - Quiz result records
- `tv_users` - TV device users (external reference)
- `wifi_networks` - WiFi network records
- `connected_tvs` - Connected TV devices

## Schema Management
Flyway was removed (commit `30c6fed`). Schema is managed by:
1. `sql/schema.sql` — creates both schemas + idempotently backfills `subjects.display_order`, `users.user_type`, `questions.version_group_id`/`version_number` (runs before Hibernate so regular `ADD COLUMN` via `ddl-auto` never hits populated tables).
2. Hibernate `ddl-auto: update` — applies entity changes to the active profile's schema.

## Question Bank
- **Full process & rules**: see [`QUESTION_BANK_GUIDE.md`](QUESTION_BANK_GUIDE.md) — structure, authorship rules, difficulty ramp, and how to author/load content for new subjects or grades.
- `content/seed/QuestionBankContent.java` — curated source of truth bank. Bands: **Nursery** (age 3), **Junior KG/LKG** (4), **Sr KG/UKG** (5), **Class 1–10** (6–15). Every band has four subjects: **Math, English, EVS, Hindi**. Content ramps from counting/shapes for juniors to board-level (algebra, trigonometry, electricity, civics) for upper classes; follows the common CBSE/ICSE core on the board-agnostic `ALL` board.
- **The backend starts empty**: startup seeding is disabled (`app.catalog-seeding.enabled: false`). Content is loaded on demand via **`POST /api/v1/questions/load`** (`QuestionBankLoader`), which auto-creates Board → ClassGrade → Subject → ContentPack → Quiz → Question from items carrying `boardCode`/`className`/`age`/`subject`.
- `QuizBundleSeeder` fills freemium quizzes lazily from the curated bank; any class without a curated band is topped up from a real **fallback bank** so a session never starts empty.
- **Subject ordering drives listing, not a cap**: `QuizBundleService` issues **one freemium quiz per active subject that has questions**, ordered by `Subject.displayOrder` (then id). There is no 2-quiz-per-class cap. `SubjectDisplayOrderInitializer` assigns curated defaults (Math=1, EVS=2, English=3, Hindi=4, General Knowledge=5, others=10) idempotently at startup (only where displayOrder is still 0). Rebuild issued bundles with `POST /api/v1/quiz-bundles/rebuild-catalog` (ADMIN) or `FREEMIUM_REBUILD_ON_STARTUP=true`.
- Delivery packs follow the **freemium naming convention** (`Freemium <Subject>`). `QuestionBankLoader` resolves packs to that convention (reusing an existing freemium pack, converting an auto-created `Loaded <Subject>` pack in place, or creating a fresh one), and `QuizBundleService`/`QuizBundleSeeder` select the freemium-named active pack — falling back to any other active pack — so bank-loaded quizzes are always served by the bundle, never invisible.
- Filtering: bundles are per class (via ClassGrade → Subjects → ContentPacks → Quizzes). If a bundle request omits `className` and supplies `age`, the class band is derived from the child's age.
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
