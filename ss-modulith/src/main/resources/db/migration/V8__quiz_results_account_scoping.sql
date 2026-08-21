-- V7: Scope quiz results to the account that saved them.
-- Nullable so pre-existing rows (saved before scoping existed) remain loadable;
-- unowned rows are treated as invisible to all accounts on read.
ALTER TABLE quiz.quiz_results ADD COLUMN account_id BIGINT;

CREATE INDEX idx_quiz_results_account_id ON quiz.quiz_results (account_id);
