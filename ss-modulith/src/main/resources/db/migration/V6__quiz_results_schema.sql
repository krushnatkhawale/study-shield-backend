-- V6: Quiz results module schema
CREATE TABLE quiz.quiz_results (
    id BIGSERIAL PRIMARY KEY,
    child_name VARCHAR(255) NOT NULL,
    score INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    time_spent_seconds BIGINT NOT NULL,
    content_name VARCHAR(255),
    category VARCHAR(255),
    completed_at TIMESTAMP NOT NULL DEFAULT now(),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
