# Academic Structure — Global Class Matrix & Offerings

> Status: **active**. Content is anchored to **offerings** (`board_class_subject`), which are
> (board + class ordinal + global subject). Display names ("Class 3", "Grade 3", "Year 4")
> are board-local presentation only — they are **never** part of identity.

## Spine: `class_levels`

A single global ordinal spine (the *age band*), defined in `AcademicStructureSeeder`:

| ordinal | age (years) | canonical name | stage |
|--------:|------------:|----------------|-------|
| 1  | 3   | Playgroup  | early-learning |
| 2  | 4   | Nursery    | early-learning |
| 3  | 5   | Junior KG  | early-learning |
| 4  | 6   | Senior KG  | early-learning |
| 5  | 7   | Class 1    | primary |
| 6  | 8   | Class 2    | primary |
| 7  | 9   | Class 3    | primary |
| 8  | 10  | Class 4    | primary |
| 9  | 11  | Class 5    | primary |
| 10 | 12  | Class 6    | middle |
| 11 | 13  | Class 7    | middle |
| 12 | 14  | Class 8    | middle |
| 13 | 15  | Class 9    | secondary |
| 14 | 16  | Class 10   | secondary |
| 15 | 17  | Class 11   | secondary |
| 16 | 18  | Class 12   | secondary |
| 17 | —   | Class 13   | post-secondary |

**Same ordinal = same age band**, nothing more. India "Class n" ↔ ordinal `n+4`;
ENG "Year n" ↔ ordinal `n+4`; US "Grade n" ↔ ordinal `n+4`.

## Boards seeded at boot

`AcademicStructureSeeder` runs once per boot (`@Order(1)`, gated by
`app.academic-structure-seeding.enabled`, default true). It is idempotent — rows are
created only when missing.

| board | country | min/max ordinal | labels |
|-------|---------|-----------------|--------|
| ALL   | —       | 2–16            | generic canonical labels |
| CBSE  | IN      | 1–16            | Playgroup, Nursery, Junior KG, Senior KG, Class 1 … Class 12 |
| MH    | IN      | 1–16            | same India scheme |
| ICSE  | IN      | 1–16            | same India scheme |
| ENG   | GB      | 2–16            | Nursery (2,3), Reception (4), Year n (5–16 = `n−4`) |
| US    | —       | 2–16            | Preschool (2,3), Kindergarten (4), Grade n (5–16 = `n−4`) |
| IB    | —       | 2–16            | PYP/MYP/DP programmes with "(Class n)" suffixes |

Board/level identity is UNIQUE `(board_id, class_level_id)` (`uk_board_class_board_level`).
Duplicate display labels within a board are permitted (ENG "Nursery" on ordinals 2 & 3,
US "Preschool" on 2 & 3) — label lookups must tolerate duplicates
(`BoardClassRepository.findByBoardBoardCodeAndDisplayNameEnumerable` returns a list).

> **Enum vs formula**: the README matrix uses India Year 12 = ordinal 12 by that n+4
> convention; where the label text and the table disagreed, the **formula wins** (ENG
> "Year 12" is ordinal 16 = Class 12 age band; there is no ordinal 15 "Year 12" label).

## Offerings: `board_class_subject`

An offering = a board-class row joined to a global subject. It is the anchor for every
`content_pack.offering_id`, every `quiz_bundle.offering_id`, and every resolved request.

v1 seed coverage:

- **ALL**: Math, English, EVS on every ordinal; + Science, Social Studies from ordinal ≥ 10.
- **CBSE**: Math, English, EVS on every ordinal; + Science, Social Studies from ordinal ≥ 10.
- **MH, ICSE**: board_class labels only — no offerings yet (requests fall back to ALL).
- **ENG, US, IB**: board_class labels only — no offerings yet.

The ALL fallback in `AcademicCatalogResolver.resolveOfferings`: when a board has no
offering at an ordinal, the generic ALL offerings for that ordinal are used. That is safe
because ALL carries only age-band content; board-specific syllabi are never mixed in.

## Global subjects

`subjects` is global (`code` UNIQUE). Seeded: MATH 1, ENG 2, EVS 3, SCI 4, SST 5, HI 6,
OTHER 7. Requests auto-create a global subject from the name if it does not exist.

## Adding a board

Adding a board is **data, not code** (unless you also want labels/countries curated):

```sql
-- 1) the board (country optional; min/max cover must equal the matrix span)
INSERT INTO "ss-dev".boards
  (name, code, description, active, country_id, min_ordinal, max_ordinal,
   created_at, updated_at)
VALUES
  ('Tamil Nadu State Board', 'TN', 'TN board', true,
   (SELECT id FROM "ss-dev".countries WHERE iso2 = 'IN'),
   1, 16, now(), now());

-- 2) one board_class row per covered level (India scheme: Playgroup=1 … Class 12=16)
INSERT INTO "ss-dev".board_class (board_id, class_level_id, display_name, created_at, updated_at)
SELECT b.id, cl.id, cl.canonical_name, now(), now()
FROM "ss-dev".boards b
JOIN "ss-dev".class_levels cl ON cl.ordinal BETWEEN 1 AND 16
WHERE b.code = 'TN'
ON CONFLICT ON CONSTRAINT uk_board_class_board_level DO NOTHING;

-- 3) optionally seed offerings (here: core three subjects at every level)
INSERT INTO "ss-dev".board_class_subject (board_class_id, subject_id)
SELECT bc.id, s.id
FROM "ss-dev".board_class bc
JOIN "ss-dev".boards b ON b.id = bc.board_id
JOIN "ss-dev".class_levels cl ON cl.id = bc.class_level_id
JOIN "ss-dev".subjects s ON s.code IN ('MATH', 'ENG', 'EVS')
WHERE b.code = 'TN' AND cl.ordinal BETWEEN 1 AND 16
ON CONFLICT ON CONSTRAINT uk_board_class_subject_offering DO NOTHING;
```

Content then arrives through the normal pipeline: `POST /api/v1/questions/load` with
`boardCode: "TN"` (or manual `content_pack`/`quiz`/`question` writes against the offering),
and a bundle request for `boardCode: "TN"` serves it. Restart finishes the job by aligning
`boards.min_ordinal/max_ordinal` with the matrix (only widened, never narrowed).

## className resolution order (`AcademicCatalogResolver`)

1. Board's own `board_class` labels (case-insensitive) — duplicate labels resolve to the
   lowest ordinal.
2. The generic ALL board's labels.
3. Heuristic: `Playgroup→1`, `Nursery/Trial/Exp/Promo→2`, `LKG/Junior→3`,
   `UKG/Senior/Reception/Kindergarten→4`, `Preschool→2`, `Class|Grade|Year| Std n→ n+4`.

## Legacy migration (`schema.sql`)

On databases that still carry `content_packs.subject_id`, startup:
1. Wipes the content chain: `questions`, `quizzes`, `content_packs`, `quiz_bundles`,
   `subjects` (CASCADE).
2. Drops `subjects.class_grade_id`, drops table `class_grades`.
3. Pre-adds `boards.min_ordinal`/`max_ordinal` (defaults 1/16) so `ddl-auto` never fails
   `ADD COLUMN NOT NULL` on a populated table.

`quiz_attempts`, `attempt_answers`, `question_feedback`, `quiz_results`, and
`child_profiles` keep their plain Long references by design (no cascade on wipe).