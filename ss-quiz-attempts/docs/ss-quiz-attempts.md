# Quiz Attempt Service - Detailed Description

## Purpose
Handle parent-initiated quiz sessions and results.

## Key Responsibilities
- Record when a parent starts a quiz on TV
- Accept answers (single or multiple questions)
- Calculate scores
- Store session results

## Current App Adaptation Note
Build this as an enhancement on top of any existing quiz functionality. Support incremental rollout (first simple quiz recording, later full session management).

## Main Entities
- QuizAttempt
- AttemptAnswer
- QuizResult

## Important
- No automatic triggering — only parent-started quizzes.