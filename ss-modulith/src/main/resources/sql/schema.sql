CREATE SCHEMA IF NOT EXISTS "ss-dev";

-- Backfill display_order on existing subjects tables. This runs before Hibernate's
-- ddl-auto update, which would otherwise fail with a NOT NULL ADD COLUMN on a table
-- that already has rows. Idempotent: no-ops when subjects is absent (fresh DB) or when
-- the column already exists.
ALTER TABLE IF EXISTS "ss-dev".subjects ADD COLUMN IF NOT EXISTS display_order integer NOT NULL DEFAULT 0;
