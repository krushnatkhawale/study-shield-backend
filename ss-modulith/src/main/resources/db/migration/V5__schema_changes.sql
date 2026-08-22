-- V5: Schema changes
-- 1. Drop grade_number from class_grades (name is the source of truth)
ALTER TABLE content.class_grades DROP COLUMN grade_number;

-- 2. Rename freemium_packs → quiz_bundles
ALTER TABLE content.freemium_packs RENAME TO quiz_bundles;
ALTER INDEX content.idx_freemium_packs_key RENAME TO idx_quiz_bundles_key;

-- 3. Add superseded_by_id to questions (immutable question versioning)
ALTER TABLE content.questions ADD COLUMN superseded_by_id BIGINT;
ALTER TABLE content.questions ADD CONSTRAINT fk_questions_superseded_by
    FOREIGN KEY (superseded_by_id) REFERENCES content.questions(id);

-- 4. Add version column for optimistic locking
ALTER TABLE user_.users ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
ALTER TABLE quiz.quiz_attempts ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
