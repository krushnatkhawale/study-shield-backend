-- Cleanup script for leftover objects in ss-dev schema
-- This should be run after confirming all objects are properly migrated to ss-dev

-- Drop any legacy tables that might have been left over from previous implementations
-- These tables are not part of the new academic structure model

-- Note: Before running this cleanup, ensure all required data has been moved and all references updated.
-- This is a safety script - use carefully!

-- List of tables that should be dropped if they exist in ss-dev schema
-- (These would be legacy class/grade related tables that are replaced by the new structure)

DROP TABLE IF EXISTS "ss-dev".class_grades CASCADE;
DROP TABLE IF EXISTS "ss-dev".grades CASCADE;
DROP TABLE IF EXISTS "ss-dev".subjects_old CASCADE;
DROP TABLE IF EXISTS "ss-dev".content_packs CASCADE;  -- This table should be handled differently now as we have offering_id
DROP TABLE IF EXISTS "ss-dev".quizzes CASCADE;  -- This table should be handled differently now as we have offering_id 
DROP TABLE IF EXISTS "ss-dev".quiz_bundles CASCADE;  -- This table should be handled differently now as we have offering_id

-- Drop any views that were part of the old structure
DROP VIEW IF EXISTS "ss-dev".class_grade_subject_view CASCADE;
DROP VIEW IF EXISTS "ss-dev".subject_class_grade_view CASCADE;

-- Drop any sequences that may have been used by legacy tables
DROP SEQUENCE IF EXISTS "ss-dev".class_grades_id_seq CASCADE;
DROP SEQUENCE IF EXISTS "ss-dev".grades_id_seq CASCADE;
DROP SEQUENCE IF EXISTS "ss-dev".subjects_old_id_seq CASCADE;