# ss-modulith - Study Shield Modulith

## Overview
Single deployable Spring Boot application containing all business modules. Replaces the previous 5-microservice architecture for cost optimization on Render.com free tier.

## Architecture
- **Pattern**: Spring Modulith (modular monolith)
- **Port**: 8080 (single JVM)
- **Database**: Single PostgreSQL with schemas per module
- **Schema Management**: Flyway migrations (V1-V5)

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

### tv/ (TV Device Module)
- **Entities**: User (tv_users), WifiNetwork, ConnectedTV
- **Controllers**: UserController, WifiNetworkController, ConnectedTVController
- **API Paths**: `/api/v1/wifi-networks/**`, `/api/v1/connected-tvs/**`, `/api/v1/tv-users/**`
- **Dependencies**: None (uses external IDs)

### shared/ (Cross-Module Interfaces)
- `ContentReference` - Interface for content module lookups
- `UserReference` - Interface for user module lookups

## Database Schema

### content schema
- `boards` - Board definitions
- `class_grades` - Grade levels per board (name is source of truth, no grade_number)
- `subjects` - Subjects per grade
- `content_packs` - Content packages per subject
- `quizzes` - Quiz definitions
- `questions` - Quiz questions with JSON options, superseded_by_id for versioning
- `quiz_bundles` - Idempotent quiz bundle downloads (renamed from freemium_packs)

### user_ schema
- `users` - User accounts (email, password, role, version for optimistic locking)
- `parent_profiles` - Parent profile details
- `child_profiles` - Child/student profiles

### quiz schema
- `quiz_attempts` - Quiz session tracking (version for optimistic locking)
- `attempt_answers` - Individual answer records

### tv schema
- `tv_users` - TV device users (external reference)
- `wifi_networks` - WiFi network records
- `connected_tvs` - Connected TV devices

## Flyway Migrations
- `V1__content_schema.sql` - Content module tables
- `V2__user_schema.sql` - User module tables
- `V3__quiz_schema.sql` - Quiz attempts tables
- `V4__tv_schema.sql` - TV device tables
- `V5__schema_changes.sql` - Drop grade_number, rename freemium_packs→quiz_bundles, add superseded_by_id, add version columns

## Question Bank (issue #1)
- `content/seed/QuestionBankContent.java` — curated seed bank: **Sr KG** and **Class 1–10**, four subjects each (Math, EVS, English, General Knowledge), 10 questions per subject per class (~475 total). Class 2–10 difficulty ramps to board level (algebra, trigonometry, electricity, civics); content follows the common CBSE/ICSE core on the board-agnostic ALL board.
- `QuizBundleSeeder` fills freemium quizzes from the curated bank (3 questions per quiz for known bands); any class without a curated band is topped up from a real **fallback bank** so a session never starts empty.
- Filtering: bundles are per class (via ClassGrade → Subjects → ContentPacks → Quizzes). If a bundle request omits `className` and supplies `age`, the class band is derived from the child's age (≤5 → Sr KG, ≤7 → Class 1).
- A freemium quiz needs ≥ 3 active questions (`QuizBundleService.MIN_ACTIVE_QUESTIONS_PER_QUIZ`) for a session to start; otherwise it fails fast unless `allowPartial=true`.
- Adding questions later: append to `QuestionBankContent` or use the content admin APIs (`/api/v1/questions`).

## Configuration
- `spring.jpa.hibernate.ddl-auto=validate` (Flyway manages schema)
- `spring.flyway.schemas=public,content,user_,quiz,tv`
- `spring.datasource.hikari.maximum-pool-size=2`
- `spring.security.jwt.secret=${JWT_SECRET}`
- `app.jwt.expiration-ms=86400000` (24 hours)

## Deployment
- **Dockerfile**: Multi-stage build (eclipse-temurin:21-jdk-jammy → eclipse-temurin:21-jre-jammy)
- **Health Check**: `/actuator/health`
- **Render.com**: Single Docker Web Service, ~150 hours/month (free tier)
