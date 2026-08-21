-- V9: Heal databases that missed V5 (blocked by the duplicate-V7 startup failure).
-- grade_number was dropped in V5 (name is the source of truth); some environments
-- still carry the NOT NULL column, which breaks inserts that don't map it.
ALTER TABLE content.class_grades DROP COLUMN IF EXISTS grade_number;
