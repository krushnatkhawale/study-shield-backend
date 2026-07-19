-- Full schema recreation for ss-user-service
-- Drops and recreates all tables on every startup

-- Drop tables in dependency order
DROP TABLE IF EXISTS child_profiles CASCADE;
DROP TABLE IF EXISTS parent_profiles CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(255),
    role VARCHAR(255) NOT NULL DEFAULT 'PARENT',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- parent_profiles table
CREATE TABLE parent_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    gender VARCHAR(255),
    relation VARCHAR(255),
    type VARCHAR(255) NOT NULL DEFAULT 'ACCOUNT_HOLDER',
    is_default BOOLEAN NOT NULL DEFAULT false,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- child_profiles table
CREATE TABLE child_profiles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INTEGER NOT NULL DEFAULT 0,
    user_id BIGINT NOT NULL REFERENCES users(id),
    board_id BIGINT,
    class_grade_id BIGINT,
    gender VARCHAR(255),
    birth_year INTEGER,
    student_class VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
