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
ALTER TABLE IF EXISTS "ss-dev".users  ADD COLUMN IF NOT EXISTS user_type varchar(16) NOT NULL DEFAULT 'MOBILE';
ALTER TABLE IF EXISTS "ss-prod".users ADD COLUMN IF NOT EXISTS user_type varchar(16) NOT NULL DEFAULT 'MOBILE';
UPDATE "ss-dev".users  SET user_type = 'ADMIN' WHERE role = 'ADMIN' AND user_type <> 'ADMIN';
UPDATE "ss-prod".users SET user_type = 'ADMIN' WHERE role = 'ADMIN' AND user_type <> 'ADMIN';

-- Question versioning: every version of a question shares a version_group_id and carries an
-- ascending version_number. A row whose superseded_by_id is NULL is the latest version for its
-- quiz ("always use the latest version of a question for a quiz"). Idempotent: adds the columns
-- once so Hibernate's ddl-auto update can pick them up on existing DBs.
ALTER TABLE IF EXISTS "ss-dev".questions  ADD COLUMN IF NOT EXISTS version_group_id varchar(64);
ALTER TABLE IF EXISTS "ss-dev".questions  ADD COLUMN IF NOT EXISTS version_number integer NOT NULL DEFAULT 1;
ALTER TABLE IF EXISTS "ss-prod".questions ADD COLUMN IF NOT EXISTS version_group_id varchar(64);
ALTER TABLE IF EXISTS "ss-prod".questions ADD COLUMN IF NOT EXISTS version_number integer NOT NULL DEFAULT 1;