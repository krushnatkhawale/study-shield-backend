# Content Service - Detailed Description

## Purpose
Central service for all educational content (boards → grades → subjects → packs → quizzes → questions).

## Key Responsibilities
- Manage education boards (CBSE, ICSE, State Boards, future international)
- Subjects and class/grades
- Content packs (freemium pack container per subject)
- Quiz and question management aligned with **mobile app asset schema**

## App alignment

Mobile assets (`QuizFileQuestion`) and TCP play path expect:

| Field | Notes |
|-------|--------|
| `resourceId` | Stable id e.g. `c01_q01` |
| `questionText` / `questionImageUrl` | Stem + optional image |
| `questionType` | `SINGLE_CHOICE`, `MULTIPLE_CHOICE`, `TRUE_FALSE`, `FITB` |
| `options[]` | `{ id, text, imageUrl }` — **not** fixed A/B/C/D columns |
| `correctAnswers[]` | Option ids, or free-text for FITB |
| `points`, `difficulty`, `languages`, `tags` | Metadata |
| Quiz unit | ~**10** questions (STANDARD); SINGLE = 1 for young kids |
| Freemium | **5 quizzes per subject** (`freemiumIndex` 1..5), tier `FREEMIUM` |

Hierarchy for delivery (not a flat bank dump):

```
Board → ClassGrade → Subject → ContentPack → Quiz (×5 freemium) → Question (×10)
```

Mobile downloads quizzes (with questions), never the entire bank.

## Entities
| Entity | Table | Description |
|--------|-------|-------------|
| Board | boards | Education boards (CBSE, ICSE) |
| ClassGrade | class_grades | Grade/class within a board |
| Subject | subjects | Subject tied to a class grade |
| ContentPack | content_packs | Collection for a subject (e.g. freemium pack) |
| Quiz | quizzes | STANDARD/SINGLE, tier, freemiumIndex, language, questionCount |
| Question | questions | Full app-shaped MCQ/FITB with JSON options + correctAnswers |

## Question JSON shape (API)

```json
{
  "resourceId": "c01_q01",
  "questionText": "What is 2+2?",
  "questionImageUrl": null,
  "questionType": "SINGLE_CHOICE",
  "options": [
    { "id": "a", "text": "3", "imageUrl": null },
    { "id": "b", "text": "4", "imageUrl": null }
  ],
  "correctAnswers": ["b"],
  "explanation": null,
  "points": 1,
  "difficulty": "EASY",
  "languages": ["English"],
  "tags": ["addition"],
  "quizId": 1,
  "blacklisted": false,
  "orderIndex": 0
}
```

## API Endpoints
All endpoints are under `/api/v1/`:

| Resource | Endpoints |
|----------|-----------|
| Boards | `GET/POST /boards`, `GET/PUT/DELETE /boards/{id}` |
| ClassGrades | `GET/POST /class-grades`, `GET /class-grades/board/{boardId}`, `GET/PUT/DELETE /class-grades/{id}` |
| Subjects | `GET/POST /subjects`, `GET /subjects/class-grade/{classGradeId}`, `GET/PUT/DELETE /subjects/{id}` |
| ContentPacks | `GET/POST /content-packs`, `GET /content-packs/subject/{subjectId}`, `GET/PUT/DELETE /content-packs/{id}` |
| Quizzes | `GET/POST /quizzes`, `GET /quizzes/{id}` (**includes questions**), `GET /quizzes/content-pack/{id}`, `GET /quizzes/content-pack/{id}/download` (pack + questions), `PUT/DELETE /quizzes/{id}` |
| Questions | `GET/POST /questions`, `GET /questions/quiz/{quizId}`, `GET /questions/quiz/{quizId}/active`, `GET/PUT/DELETE /questions/{id}` |

## Database
- **Production**: Aiven PostgreSQL via `DATABASE_URL` / credentials
- **Tests**: H2 in-memory (`json` columns via Hibernate)
- Options / correctAnswers / languages / tags stored as **JSON**

## Deployment
- Platform: render.com
- Java version: 21+

## Design Decisions

### [2026-07-14] No Lombok
- Context: System runs Java 25, Lombok incompatible
- Decision: Use Java records for DTOs, manual getters/setters for entities

### [2026-07-14] Quiz Types
- STANDARD (default 10 questions) and SINGLE (1 question for young kids)

### [2026-07-14] Question Blacklist
- Soft-retire; active/download endpoints exclude blacklisted

### [2026-07-14] App-aligned question model
- Replaced fixed optionA–D + correctOption with options JSON + correctAnswers[]
- Added questionType, resourceId, difficulty, languages, tags, points, questionImageUrl
- Quiz: contentTier, freemiumIndex, language for freemium/premium delivery

### [2026-07-17] Freemium pack API (Option A)
- `POST /api/v1/freemium/packs` — issue or return idempotent pack for class + deviceId/childId
- `GET /api/v1/freemium/packs/{packId}` — re-download full quizzes with questions
- Product rule: **5 quizzes per subject**, **10 questions** each, `contentTier=FREEMIUM`
- Auto-seeds catalog for class if missing (Board/Class/Subjects/Packs/Quizzes/Questions)
- Gateway routes `/api/v1/freemium/**` to content-service
