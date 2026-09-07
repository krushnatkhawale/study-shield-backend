# Question Bank Process & Rules

This document records the process and rules used to author and load StudyShield's
question bank, so the same method can be reused to produce content for new subjects
or grades in the future.

## Why this exists

The question bank used to be seeded into the database at startup. That changed: the
backend now **starts empty** and catalog content is loaded **on demand**. This guide is
the canonical reference for how question content is structured, how it gets into the
database, and the rules every question must follow (they are enforced by tests, so
following them keeps the suite green).

## Source of truth

Curated content lives in a single Java source file:

```
content/seed/QuestionBankContent.java
```

It is a **static, compile-time bank** (a script path, not a runtime dependency). The
DB is populated from it (or from hand-authored `QuestionBankLoadItem`s) via the on-demand
loader — see [Loading content](#loading-content).

## Bank structure

The bank is keyed twice: `Map<band, Map<subject, List<SeedQuestion>>>`.

```
BANK = {
  "Nursery"    : { "Math": [...], "English": [...], "EVS": [...], "Hindi": [...] },
  "Junior KG"  : { ... same 4 subjects ... },   // = LKG
  "Sr KG"      : { ... same 4 subjects ... },   // = UKG
  "Class 1"    : { ... same 4 subjects ... },
  ...
  "Class 10"   : { ... same 4 subjects ... },
}
```

### Bands & age mapping

| Band (bank key)        | Typical age | Alias / notes                     |
|------------------------|-------------|-----------------------------------|
| `Nursery`              | 3           | `Trial` / `Exp` / `promo` map here|
| `Junior KG` (LKG)      | 4           | `junior`, `lkg`                   |
| `Sr KG` (UKG)          | 5           | `sr`, `senior`, `ukg`             |
| `Class 1` … `Class 10` | 6–15        | bare number / `grade N` / `std N` |

`classNameForAge(age)` derives a band from age when a request omits `className`.
`bandForClassName(name)` normalises a class name to a bank key. Both live in
`QuestionBankContent`.

### Subjects

Every band carries exactly the **four** subjects: `Math`, `English`, `EVS`, `Hindi`.
> Note: `General Knowledge` was renamed to `Hindi` (Hindi content currently uses
> English text with India-focused topics — real Hindi script is a later task).

### Question shape

`SeedQuestion(text, trueFalse, options, correct)`:
- `trueFalse` → options are exactly `["True", "False"]`.
- otherwise → **single-choice, exactly 4 options**.
- `correct` is the option **text** that is the one right answer.

## Authorship rules (enforced by tests)

These are the invariants the test `QuestionBankContentTest` checks. Any new subject or
grade content must satisfy them or the build fails.

1. **Enough total volume** — the whole bank has `>= 400` questions.
2. **All four subjects present** — every band (Class 2–10, Nursery, Junior KG, Sr KG,
   Class 1) includes `Math`, `English`, `EVS`, `Hindi`.
3. **`>= 10` questions per subject per band** — so quizzes have enough to fill up.
4. **One correct shape** — every question is either TRUE_FALSE with exactly `[True, False]`,
   or SINGLE_CHOICE with exactly 4 options.
5. **Correct answer is real** — `correct` must be one of `options`.
6. **Age-appropriate, no placeholders** — text must not contain placeholder tokens
   (`Option A`, `Sample question`) and must be longer than 8 characters.
7. **No duplicate question text within a band**.
8. **Age maps sensibly** — `classNameForAge` returns the expected band for sample ages.

### Difficulty ramp (style convention)

Authoring should follow a difficulty progression so each grade feels right:
- **Nursery / KG / Class 1**: counting, simple addition/subtraction, shapes, colours,
  body parts, first letters, opposites, rhymes. Readable, picture-friendly.
- **Class 2–5**: two/three-digit arithmetic, intro multiplication/division, money,
  plants/animals, basic grammar, national symbols.
- **Class 6–10**: board-level topics — integers, algebra, ratio, fractions, exponents,
  polynomials, trigonometry, geometry, and science/civics/geography appropriate to grade.

Content follows the common **CBSE/ICSE core** and is hosted on the board-agnostic `ALL` board.

## Loading content

The DB starts empty. Content reaches it one of three ways:

### 1. On-demand batch loader — `POST /api/v1/questions/load`
Accepts a list of `QuestionBankLoadItem`s. Each item carries enough metadata for the
loader (`content/service/QuestionBankLoader.java`) to auto-create the whole chain
**Board → ClassGrade → Subject → ContentPack → Quiz → Question**:

```jsonc
[
  {
    "boardCode": "ALL",
    "className": "Class 3",
    "subject": "Math",
    "questionType": "SINGLE_CHOICE",
    "questionText": "What is 6 × 4?",
    "options": ["24", "20", "18", "26"],
    "correctAnswer": "24",
    "orderIndex": 0
  }
]
```

Rules of the loader:
- Groups items by `(boardCode, className, subject)`.
- Creates any missing Board / ClassGrade / Subject / ContentPack / Quiz.
- Skips a question whose exact text already exists in that quiz (idempotent re-runs).
- When a quiz exceeds capacity, a new quiz is created for the overflow.
- Packs are resolved to the **freemium naming convention** (`Freemium <Subject>`) shared with
  `QuizBundleSeeder` / `QuizBundleService`, so loaded quizzes are always visible to the mobile
  bundle:
  - an existing `Freemium <Subject>` pack is reused;
  - a pack previously auto-created as `Loaded <Subject>` is renamed in place (no orphan packs);
  - otherwise a fresh `Freemium <Subject>` pack is created.

### 2. `question-bank.json` (ops bulk load)
`question-bank.json` at the repo root is the full curated bank serialised into
`QuestionBankLoadItem` format (560 questions, Nursery→Class 10, all four subjects).
Load it with:

```bash
curl -X POST "$BASE/api/v1/questions/load" \
  -H "Content-Type: application/json" \
  -d @question-bank.json
```

Regenerate it (or regenerate for a new subject/grade) by re-running a small generator
that walks `QuestionBankContent.BANK` and emits the item JSON — the file is produced from
the same source of truth, so nothing is hand-maintained.

### 3. Mobile per-login Trial seed
The mobile app seeds **only** the Trial/Nursery bank on login via the same `/questions/load`
endpoint (`TrialContentDownloader` on the mobile side). It is fire-and-forget and
repeat-safe. To push more content to every login, expand the embedded Trial bank there.

## Adding a NEW subject (change process)

To extend the bank to a brand-new subject (e.g. `Science`, `Art`), the wiring is:

1. **Author the questions** in `QuestionBankContent.java` — add a `"<Subject>"` key to the
   map for each band you want to cover, satisfying the authorship rules above.
2. **Update `QuizBundleSeeder.DEFAULT_SUBJECTS`** so freemium quiz creation covers the new
   subject (currently `Math`, `EVS`, `English`, `Hindi`).
3. **Tell the loader** nothing special is needed — `QuestionBankLoader` creates subjects
   by name automatically from the item payload.
4. **Update `QuestionBankContentTest`** if you add subjects that should be asserted present.
5. **Regenerate** `question-bank.json` so the new subject ships in the bulk load file.
6. Build + test: `./gradlew :ss-modulith:test`.

## Adding a NEW grade (change process)

1. Add a band key (e.g. `"Class 11"`) to `BANK` in `QuestionBankContent.java` with the four
   subjects and `>= 10` questions each.
2. Extend `bandForClassName` and `classNameForAge` if the new grade should be reachable
   by name or age.
3. Bump `MAX_CURATED_CLASS` if it is in the numbered class range used by tests.
4. Regenerate `question-bank.json`.

## Config flag

- `app.catalog-seeding.enabled` — when `false` (default now), the startup catalog seeder
  does **not** run; content is provided on demand via the loader. Set `true` only if you
  intentionally want startup seeding re-enabled.
