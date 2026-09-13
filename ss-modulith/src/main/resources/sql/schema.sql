-- Study Shield backend database bootstrap.
-- Runs on every startup before Hibernate's ddl-auto update (spring.sql.init.mode: always).
--
-- Two environments share one Postgres database, one schema each:
--   * default profile -> "ss-dev"   (development)
--   * 'prod' profile  -> "ss-prod"  (production)
--
-- This script is profile-independent: it creates BOTH schemas and applies the
-- same idempotent column backfills to both, so the two environments stay in
-- lock-step no matter which profile starts first.

CREATE SCHEMA IF NOT EXISTS "ss-dev";
CREATE SCHEMA IF NOT EXISTS "ss-prod";

-- Backfill display_order on existing subjects tables. This runs before Hibernate's
-- ddl-auto update, which would otherwise fail with a NOT NULL ADD COLUMN on a table
-- that already has rows. Idempotent: no-ops when subjects is absent (fresh DB) or when
-- the column already exists.
ALTER TABLE IF EXISTS "ss-dev".subjects  ADD COLUMN IF NOT EXISTS display_order integer NOT NULL DEFAULT 0;
ALTER TABLE IF EXISTS "ss-prod".subjects ADD COLUMN IF NOT EXISTS display_order integer NOT NULL DEFAULT 0;

-- Two user types: MOBILE (mobile/tv app accounts) and ADMIN (admin app accounts).
-- Idempotent: adds the column once; backfills existing ADMIN-role rows so they stay
-- admins even though the column default is MOBILE. Hibernate then picks up the column
-- via ddl-auto update.
-- The whole block is guarded by table existence: this script runs BEFORE Hibernate's
-- ddl-auto (spring.sql.init default ordering), so on a fresh database the tables do not
-- exist yet and there is nothing to backfill. ALTER ... IF EXISTS is safe by itself,
-- but the UPDATEs would fail on a missing table, so they only run when the table is there.
-- Written as DO '<text>' (not DO $$...$$) because Spring's ScriptUtils parser does not
-- understand dollar quoting.
DO 'BEGIN
    IF to_regclass(''"ss-dev".users'') IS NOT NULL THEN
        ALTER TABLE "ss-dev".users ADD COLUMN IF NOT EXISTS user_type varchar(16) NOT NULL DEFAULT ''MOBILE'';
        UPDATE "ss-dev".users SET user_type = ''ADMIN'' WHERE role = ''ADMIN'' AND user_type <> ''ADMIN'';
    END IF;
    IF to_regclass(''"ss-prod".users'') IS NOT NULL THEN
        ALTER TABLE "ss-prod".users ADD COLUMN IF NOT EXISTS user_type varchar(16) NOT NULL DEFAULT ''MOBILE'';
        UPDATE "ss-prod".users SET user_type = ''ADMIN'' WHERE role = ''ADMIN'' AND user_type <> ''ADMIN'';
    END IF;
END';

-- Academic catalog overhaul: class_grades + per-class grade subjects were replaced by the
-- global matrix (class_levels / boards / board_class / board_class_subject offerings).
-- Legacy databases must transition before Hibernate's ddl-auto update, because the new
-- NOT NULL/UNIQUE columns (content_packs.offering_id, quiz_bundles.offering_id,
-- subjects.code UNIQUE) cannot be added while old rows are still there:
--   * legacy subjects are duplicated per class grade (per-class subject codes) -> wipe subjects.
--   * quiz_bundles keys embedded display names ("Class 3") -> stale, wipe bundles.
--   * the whole content chain (content_packs -> quizzes -> questions) is repointed to
--     offerings -> wiped so it can be reseeded from the matrix.
-- Guard: the transition runs only when a legacy content_packs.subject_id column exists.
-- QuizAttempt/AttemptAnswer/QuestionFeedback/QuizResult/ChildProfile store referenced ids
-- as plain Long columns, so they are untouched by the wipe.
DO 'BEGIN
    IF to_regclass(''"ss-dev".content_packs'') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = ''ss-dev'' AND table_name = ''content_packs'' AND column_name = ''subject_id'') THEN
        TRUNCATE TABLE "ss-dev".questions, "ss-dev".quizzes, "ss-dev".content_packs,
            "ss-dev".quiz_bundles, "ss-dev".subjects CASCADE;
        ALTER TABLE "ss-dev".subjects      DROP COLUMN IF EXISTS class_grade_id;
        ALTER TABLE "ss-dev".content_packs DROP COLUMN IF EXISTS subject_id;
        DROP TABLE IF EXISTS "ss-dev".class_grades CASCADE;
    END IF;
    IF to_regclass(''"ss-prod".content_packs'') IS NOT NULL
       AND EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_schema = ''ss-prod'' AND table_name = ''content_packs'' AND column_name = ''subject_id'') THEN
        TRUNCATE TABLE "ss-prod".questions, "ss-prod".quizzes, "ss-prod".content_packs,
            "ss-prod".quiz_bundles, "ss-prod".subjects CASCADE;
        ALTER TABLE "ss-prod".subjects      DROP COLUMN IF EXISTS class_grade_id;
        ALTER TABLE "ss-prod".content_packs DROP COLUMN IF EXISTS subject_id;
        DROP TABLE IF EXISTS "ss-prod".class_grades CASCADE;
    END IF;
END';

-- Boards now carry a NOT NULL ordinal range (min_ordinal/max_ordinal). Pre-add the columns
-- with defaults (1..16) so Hibernate's ddl-auto cannot fail ADD COLUMN NOT NULL on a
-- populated boards table. The boot seeder then aligns values with the matrix.
DO 'BEGIN
    IF to_regclass(''"ss-dev".boards'') IS NOT NULL THEN
        ALTER TABLE "ss-dev".boards ADD COLUMN IF NOT EXISTS min_ordinal integer NOT NULL DEFAULT 1;
        ALTER TABLE "ss-dev".boards ADD COLUMN IF NOT EXISTS max_ordinal integer NOT NULL DEFAULT 16;
    END IF;
    IF to_regclass(''"ss-prod".boards'') IS NOT NULL THEN
        ALTER TABLE "ss-prod".boards ADD COLUMN IF NOT EXISTS min_ordinal integer NOT NULL DEFAULT 1;
        ALTER TABLE "ss-prod".boards ADD COLUMN IF NOT EXISTS max_ordinal integer NOT NULL DEFAULT 16;
    END IF;
END';

-- Question versioning: every version of a question shares a version_group_id and carries an
-- ascending version_number. A row whose superseded_by_id is NULL is the latest version for its
-- quiz ("always use the latest version of a question for a quiz"). Idempotent: adds the columns
-- once so Hibernate's ddl-auto update can pick them up on existing DBs.
ALTER TABLE IF EXISTS "ss-dev".questions  ADD COLUMN IF NOT EXISTS version_group_id varchar(64);
ALTER TABLE IF EXISTS "ss-dev".questions  ADD COLUMN IF NOT EXISTS version_number integer NOT NULL DEFAULT 1;
ALTER TABLE IF EXISTS "ss-prod".questions ADD COLUMN IF NOT EXISTS version_group_id varchar(64);
ALTER TABLE IF EXISTS "ss-prod".questions ADD COLUMN IF NOT EXISTS version_number integer NOT NULL DEFAULT 1;