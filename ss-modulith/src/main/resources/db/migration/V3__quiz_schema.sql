-- V3: Quiz attempts module schema
CREATE SCHEMA IF NOT EXISTS quiz;

CREATE TABLE quiz.quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    child_profile_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(255) NOT NULL DEFAULT 'IN_PROGRESS',
    total_questions INTEGER,
    correct_answers INTEGER,
    score INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE quiz.attempt_answers (
    id BIGSERIAL PRIMARY KEY,
    quiz_attempt_id BIGINT NOT NULL REFERENCES quiz.quiz_attempts(id),
    question_id BIGINT NOT NULL,
    selected_option VARCHAR(255) NOT NULL,
    correct BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
