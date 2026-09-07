CREATE SCHEMA IF NOT EXISTS "ss-dev";

-- Backfill display_order on existing subjects tables. This runs before Hibernate's
-- ddl-auto update, which would otherwise fail with a NOT NULL ADD COLUMN on a table
-- that already has rows. Idempotent: no-ops when subjects is absent (fresh DB) or when
-- the column already exists.
ALTER TABLE IF EXISTS "ss-dev".subjects ADD COLUMN IF NOT EXISTS display_order integer NOT NULL DEFAULT 0;

-- Two user types: MOBILE (mobile/tv app accounts) and ADMIN (admin app accounts).
-- Idempotent: adds the column once; backfills existing ADMIN-role rows so they stay
-- admins even though the column default is MOBILE. Hibernate then picks up the column
-- via ddl-auto update.
ALTER TABLE IF EXISTS "ss-dev".users ADD COLUMN IF NOT EXISTS user_type varchar(16) NOT NULL DEFAULT 'MOBILE';
UPDATE "ss-dev".users SET user_type = 'ADMIN' WHERE role = 'ADMIN' AND user_type <> 'ADMIN';
