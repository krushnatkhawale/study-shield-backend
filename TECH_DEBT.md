# Tech Debt — study-shield-backend

## TD-1: Move freemium catalog seeding out of the request path

**Added:** 2026-08-21
**Source:** HikariCP "apparent connection leak" warnings + 433-second `POST /api/v1/quiz-bundles` in production logs.

### Problem

`QuizBundleService.createBundle` calls `QuizBundleSeeder.ensureCatalogForClass`
synchronously inside the request transaction. For a class seeded for the first
time this creates the class grade, subjects, content packs, up to 25 quizzes and
~250 questions row-by-row over the network (Render latency), holding one pooled
DB connection for minutes:

- HikariCP leak detection (5s threshold) fires on every first-hit, producing
  misleading "Apparent connection leak" stack traces.
- Concurrent first requests for the same class can lock-wait against each other;
  a related race on `idx_quiz_bundles_key` was already fixed in `a29bfda`.
- First user per class waits minutes for a response.

### Recommended fix

Pre-seed the catalog at startup for known bands/classes (Nursery → Grade 10,
deterministic), or seed asynchronously on first miss so request transactions stay
sub-second. Optionally batch question inserts (`saveAll` is used, but each option
row still round-trips) and raise `leakDetectionThreshold` only if warnings persist
after the move.

### Acceptance

- `POST /api/v1/quiz-bundles` completes in < 2s even for an unseen class.
- No HikariCP leak warnings during seeding.

## TD-4: Migrations assumed stale `public.*` tables; seeding now flag-gated (2026-08-22)

Fresh-database rebuild exposed two issues:

- V5 and V7 referenced `public.users` / `public.quiz_attempts` — tables that only
  existed because Hibernate `ddl-auto: update` had created them before Flyway was
  introduced. Fixed to point at the canonical `user_.users` and `quiz.quiz_attempts`.
  The dev DB was dropped (`content`, `user_`, `quiz`, `tv`, `public` schemas) and
  rebuilt from V1–V9; all migrations now run clean on an empty database.
- `CatalogStartupSeeder` is gated by `app.catalog-seeding.enabled` (default `true`);
  the dev DB currently runs with it disabled per product decision — catalog is empty
  until data is set up deliberately.

### Follow-ups

- Hibernate re-creates duplicate copies of every table in `public` because entities
  don't pin a schema. Set the default schema or add `@Table(schema=...)` and switch
  `ddl-auto` to `validate`.
- `POST /api/v1/users` accepts malformed emails (201) — add bean validation.
- Regression scenarios "Issue quiz bundle seeds catalog..." assert auto-seeding;
  update them for the flag-gated behavior.
