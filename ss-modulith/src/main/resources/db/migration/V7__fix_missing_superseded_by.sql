-- V7: Ensure superseded_by_id exists on questions (V5 may have partially failed)
DO $$ BEGIN
    ALTER TABLE content.questions ADD COLUMN superseded_by_id BIGINT;
EXCEPTION WHEN duplicate_column THEN
    -- already exists
END $$;

DO $$ BEGIN
    ALTER TABLE content.questions ADD CONSTRAINT fk_questions_superseded_by
        FOREIGN KEY (superseded_by_id) REFERENCES content.questions(id);
EXCEPTION WHEN duplicate_object THEN
    -- already exists
END $$;

-- Ensure version columns exist (V5 may have partially failed)
DO $$ BEGIN
    ALTER TABLE user_.users ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
EXCEPTION WHEN duplicate_column THEN
    -- already exists
END $$;

DO $$ BEGIN
    ALTER TABLE quiz.quiz_attempts ADD COLUMN version INTEGER NOT NULL DEFAULT 0;
EXCEPTION WHEN duplicate_column THEN
    -- already exists
END $$;
