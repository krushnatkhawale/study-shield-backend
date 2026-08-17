-- V1: Content module schema
CREATE SCHEMA IF NOT EXISTS content;

CREATE TABLE content.boards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    code VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE content.class_grades (
    id BIGSERIAL PRIMARY KEY,
    grade_number INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    board_id BIGINT NOT NULL REFERENCES content.boards(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE content.subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    class_grade_id BIGINT NOT NULL REFERENCES content.class_grades(id),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE content.content_packs (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    subject_id BIGINT NOT NULL REFERENCES content.subjects(id),
    version INTEGER NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE content.quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    content_pack_id BIGINT NOT NULL REFERENCES content.content_packs(id),
    quiz_type VARCHAR(16) NOT NULL DEFAULT 'STANDARD',
    question_count INTEGER NOT NULL DEFAULT 10,
    content_tier VARCHAR(16) NOT NULL DEFAULT 'FREEMIUM',
    freemium_index INTEGER,
    language VARCHAR(64) NOT NULL DEFAULT 'English',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_quizzes_content_pack_id ON content.quizzes(content_pack_id);

CREATE TABLE content.questions (
    id BIGSERIAL PRIMARY KEY,
    resource_id VARCHAR(64),
    question_text TEXT NOT NULL,
    question_image_url TEXT,
    question_type VARCHAR(32) NOT NULL DEFAULT 'SINGLE_CHOICE',
    correct_option VARCHAR(4) NOT NULL DEFAULT 'A',
    optiona VARCHAR(255) NOT NULL DEFAULT '',
    optionb VARCHAR(255) NOT NULL DEFAULT '',
    optionc VARCHAR(255) NOT NULL DEFAULT '',
    optiond VARCHAR(255) NOT NULL DEFAULT '',
    optiona_image VARCHAR(500),
    optionb_image VARCHAR(500),
    optionc_image VARCHAR(500),
    optiond_image VARCHAR(500),
    options json NOT NULL DEFAULT '[]',
    correct_answers json NOT NULL DEFAULT '[]',
    explanation TEXT,
    points INTEGER NOT NULL DEFAULT 1,
    difficulty VARCHAR(16) NOT NULL DEFAULT 'EASY',
    languages json,
    tags json,
    quiz_id BIGINT NOT NULL REFERENCES content.quizzes(id),
    blacklisted BOOLEAN NOT NULL DEFAULT false,
    order_index INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_questions_resource_id ON content.questions(resource_id);
CREATE INDEX idx_questions_quiz_id ON content.questions(quiz_id);

CREATE TABLE content.freemium_packs (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    class_name VARCHAR(64) NOT NULL,
    language VARCHAR(64) NOT NULL DEFAULT 'English',
    board_code VARCHAR(64) DEFAULT 'all',
    device_id VARCHAR(64),
    child_id BIGINT,
    user_id BIGINT,
    quiz_ids json NOT NULL DEFAULT '[]',
    subjects json NOT NULL DEFAULT '[]',
    quiz_count INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_freemium_packs_key ON content.freemium_packs(idempotency_key);
