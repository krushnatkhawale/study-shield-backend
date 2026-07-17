# StudyShield Backend — API Regression Suite (Component Spec)

**Document type:** Component description + implementation brief for an expert QA automation engineer  
**Module name (proposed):** `ss-regression-suite`  
**Framework:** **Java + Cucumber (Gherkin) BDD** black-box API suite  
**Audience:** Implementing LLM / QA engineer / CI owner  
**Status:** Spec only (not implemented)  
**Version:** 1.1  
**Date:** 2026-07-15  

---

## 1. Purpose

Build a **dedicated regression suite module** that exercises the **entire StudyShield backend through the API Gateway only** (port **8080**), as a mobile client would.

The suite is a **Java-based Cucumber / Gherkin QA project**:

- Business-readable **`.feature`** files (Given / When / Then)
- Java **step definitions** + shared **World** / context
- HTTP calls via gateway (REST Assured or equivalent)
- **Report generation always enabled** (Cucumber HTML/JSON + JUnit XML + Allure)

Goals:

1. **Catch breakages** after any service deploy, schema change, or gateway route change.
2. **Validate cross-service user journeys** (content → user → TV device → quiz attempt), not only unit CRUD.
3. **Living documentation** via Gherkin that product/QA can read and review.
4. **Produce rich reports** every run (local + CI) — human HTML and machine-readable JSON/XML.
5. **Remain free-tier friendly**: run against local stack or Render staging.
6. **Stay independent** of service unit tests (those stay inside each `ss-*` module).

Non-goals (v1):

- UI / mobile Espresso or TV instrumentation
- Load/performance testing beyond light concurrency smoke
- Security pen-testing (basic authz/negative cases only)
- Direct DB assertions (prefer API-observable state; optional later)

---

## 2. System under test (SUT)

### 2.1 Architecture

```
                    ┌──────────────────────────────────────────┐
                    │  ss-regression-suite                     │
                    │  Java + Cucumber (Gherkin)               │
                    │  Features → Steps → GatewayClient        │
                    │  Reports: HTML / JSON / JUnit / Allure   │
                    └─────────────────┬────────────────────────┘
                                      │ HTTPS / HTTP
                                      │ ALL traffic via gateway
                                      ▼
                    ┌──────────────────────────────────────────┐
                    │  ss-api-gateway  :8080                   │
                    │  Spring Cloud Gateway                    │
                    └─┬─────────┬─────────┬───────────┬────────┘
                      │         │         │           │
          /api/v1/    │         │         │           │
 boards…questions     │         │         │           │
                      ▼         ▼         ▼           ▼
              content:8081  user:8082  attempts:8083  tv:8084
              Aiven PG      Aiven PG   Aiven PG       Aiven PG
```

**Hard rule:** Suite **must not** call `:8081–8084` directly in default / CI profiles.  
Direct service calls allowed only in a **`@debug`** / `direct` profile for isolating which hop failed.

### 2.2 Gateway routes (source of truth)

From `ss-api-gateway` `application.yml`:

| Route id | Downstream (default local) | Path predicates |
|----------|----------------------------|-----------------|
| content-service | `CONTENT_SERVICE_URL` → `http://localhost:8081` | `/api/v1/boards/**`, `/api/v1/class-grades/**`, `/api/v1/subjects/**`, `/api/v1/content-packs/**`, `/api/v1/quizzes/**`, `/api/v1/questions/**` |
| user-service | `USER_SERVICE_URL` → `http://localhost:8082` | `/api/v1/users/**`, `/api/v1/parents/**`, `/api/v1/children/**` |
| quiz-attempts | `QUIZ_ATTEMPTS_URL` → `http://localhost:8083` | `/api/v1/quiz-attempts/**`, `/api/v1/attempt-answers/**`, `/api/v1/quiz-results/**` |
| tv-device-service | `TV_DEVICE_SERVICE_URL` → `http://localhost:8084` | `/api/v1/wifi-networks/**`, `/api/v1/connected-tvs/**` |

Gateway management: `GET /actuator/health`, optional `GET /actuator/gateway/routes` (if exposed).

### 2.3 Ports

| Component | Port |
|-----------|------|
| API Gateway | **8080** (suite base URL) |
| Content Service | 8081 |
| User Service | 8082 |
| Quiz Attempts | 8083 |
| TV Device Service | 8084 |

### 2.4 Database

- Production/staging: **Aiven PostgreSQL**
- Suite treats DB as opaque (API-only)
- Scenarios use **unique prefixes** (timestamp / UUID) to avoid collisions on shared staging

---

## 3. Product context the suite must respect

| Principle | Test implication |
|-----------|------------------|
| Parent-initiated quizzes only | Attempt APIs create sessions explicitly; no “auto start” API expected |
| Freemium: **5 quizzes per subject** | Features seed 5 quizzes with `freemiumIndex` 1..5 |
| STANDARD quiz ≈ **10** questions | Assert `questionCount` and download payload conventions |
| Questions match mobile assets | Assert JSON: `options[]`, `correctAnswers[]`, `questionType`, `resourceId` |
| Never dump full bank to mobile | Prefer pack download / quiz-by-id with questions |
| Blacklisted questions | Active/download paths exclude `blacklisted=true` |
| Privacy (DPDP/GDPR-like) | Delete user/child flows; no cross-user leakage |

---

## 4. Locked stack: Java + Cucumber Gherkin + reports

### 4.1 Core dependencies (Gradle)

| Concern | Choice | Notes |
|---------|--------|--------|
| Language | **Java 21** | Align with monorepo |
| BDD | **io.cucumber:cucumber-java** + **cucumber-junit-platform-engine** | Gherkin features |
| Runner | **JUnit Platform** (`@Suite` / Cucumber engine) | Gradle `test` task |
| HTTP | **REST Assured** | Fluent API testing |
| Assertions | **AssertJ** (and/or Hamcrest via RestAssured) | Readable failures |
| DI / hooks (optional) | **cucumber-picocontainer** or manual World singleton | Scenario context |
| Logging | SLF4J | Request/response on failure |
| **Reports (required)** | See §12 — **always on** | Never optional in CI |

Suggested versions (pin at implement time to compatible set):

```gradle
// ss-regression-suite/build.gradle (illustrative)
dependencies {
    testImplementation platform('io.cucumber:cucumber-bom:7.20.1') // pin latest stable at impl
    testImplementation 'io.cucumber:cucumber-java'
    testImplementation 'io.cucumber:cucumber-junit-platform-engine'
    testImplementation 'io.cucumber:cucumber-picocontainer'   // optional DI

    testImplementation 'org.junit.platform:junit-platform-suite'
    testImplementation 'org.junit.jupiter:junit-jupiter'

    testImplementation 'io.rest-assured:rest-assured'
    testImplementation 'org.assertj:assertj-core'

    // Reports
    testImplementation 'io.qameta.allure:allure-cucumber7-jvm'
    testImplementation 'io.qameta.allure:allure-rest-assured'
}
```

### 4.2 Why Cucumber for this suite

| Benefit | Application |
|---------|-------------|
| Readable specs | Product journeys as Gherkin (`Freemium pack download`, `Parent starts quiz`) |
| Reuse | Shared steps: “I create a board named …”, “response status is 201” |
| Tags | `@smoke @content @regression` map to CI jobs |
| Reports | Native pretty/HTML/JSON + Allure scenario timeline |
| Living docs | Features double as acceptance criteria for backend |

### 4.3 Gherkin style guide

- Use **business language**, not raw HTTP only (HTTP details OK in steps, not mandatory in feature titles).
- Prefer **declarative** scenarios; put JSON payloads in `"""` doc strings or `data tables` / external files under `test-data/`.
- One scenario = one behaviour; use `Background` for common gateway health / prefix setup.
- Tags on **Feature** and **Scenario** level.
- Avoid UI wording; this is API QA.

**Example feature (illustrative):**

```gherkin
@smoke @gateway
Feature: API Gateway health and routing
  As a QA engineer
  I want the gateway to route and stay healthy
  So that mobile clients have a single entry point

  Background:
    Given the regression suite targets the API gateway
    And the gateway is ready within the configured timeout

  @smoke
  Scenario: Gateway health is UP
    When I GET "/actuator/health"
    Then the response status should be 200

  @gateway @content
  Scenario: Boards route is reachable via gateway
    When I GET "/api/v1/boards"
    Then the response status should be 200
    And the response body should be a JSON array
```

```gherkin
@regression @content @contract
Feature: Question payload matches mobile app contract
  As the mobile StudyShield app
  I need question JSON with options and correctAnswers
  So that TV interrupts can render and grade quizzes

  Background:
    Given a freemium content hierarchy exists for class "1" subject "Math"
    And a STANDARD quiz exists in that pack with freemium index 1

  Scenario: Create SINGLE_CHOICE question with option ids
    When I create a question with:
      """
      {
        "resourceId": "reg_c01_q01",
        "questionText": "What is 2+2?",
        "questionType": "SINGLE_CHOICE",
        "options": [
          {"id": "a", "text": "3", "imageUrl": null},
          {"id": "b", "text": "4", "imageUrl": null},
          {"id": "c", "text": "5", "imageUrl": null},
          {"id": "d", "text": "6", "imageUrl": null}
        ],
        "correctAnswers": ["b"],
        "points": 1,
        "difficulty": "EASY",
        "languages": ["English"],
        "tags": ["addition"],
        "blacklisted": false,
        "orderIndex": 0
      }
      """
    Then the response status should be 201
    And the question response must match the mobile contract
    And the question response must not contain legacy fields "optionA,optionB,optionC,optionD,correctOption"
```

```gherkin
@regression @journeys @slow
Feature: Parent-initiated quiz session via gateway
  As a parent
  I start a quiz for my child after content is available
  So that learning is intentional and tracked

  Scenario: Complete a quiz attempt with answers
    Given a parent user and child profile exist
    And a downloaded freemium quiz with at least 1 active question exists
    When the parent starts a quiz attempt for that child and quiz
    And the parent submits answers for all active questions
    And the parent completes the quiz attempt
    Then the attempt status should indicate completion
    And the attempt result should include score fields
```

---

## 5. Module layout (Cucumber-oriented)

```
study-shield-backend/
├── ss-regression-suite.md                 # THIS SPEC (root)
├── ss-regression-suite/                   # NEW Gradle module
│   ├── build.gradle
│   ├── README.md
│   ├── docs/
│   │   └── ss-regression-suite.md         # optional copy of root spec
│   ├── src/test/java/com/studyshield/regression/
│   │   ├── runners/
│   │   │   ├── CucumberSmokeTest.java     # @Suite + @IncludeTags("smoke")
│   │   │   ├── CucumberRegressionTest.java
│   │   │   └── CucumberTest.java          # full / default
│   │   ├── hooks/
│   │   │   ├── GlobalHooks.java           # @BeforeAll health, @After cleanup
│   │   │   ├── ReportingHooks.java        # attach response to Allure/Cucumber
│   │   │   └── RetryHooks.java            # optional
│   │   ├── context/
│   │   │   ├── ScenarioContext.java       # World: last response, created ids
│   │   │   ├── AuthContext.java           # future JWT
│   │   │   └── SuiteConfig.java           # env-based config
│   │   ├── client/
│   │   │   ├── GatewayClient.java
│   │   │   ├── ContentApi.java
│   │   │   ├── UserApi.java
│   │   │   ├── QuizAttemptApi.java
│   │   │   └── TvDeviceApi.java
│   │   ├── steps/
│   │   │   ├── CommonHttpSteps.java       # status, JSON path, headers
│   │   │   ├── GatewaySteps.java
│   │   │   ├── ContentSteps.java
│   │   │   ├── UserSteps.java
│   │   │   ├── QuizAttemptSteps.java
│   │   │   ├── TvDeviceSteps.java
│   │   │   └── ContractSteps.java         # mobile JSON assertions
│   │   ├── fixtures/
│   │   │   ├── ContentFixtures.java
│   │   │   ├── UserFixtures.java
│   │   │   └── PayloadFactory.java
│   │   └── support/
│   │       ├── IdRegistry.java
│   │       ├── CleanupService.java
│   │       └── JsonSchemaSupport.java
│   └── src/test/resources/
│       ├── cucumber.properties            # publish quiet, plugin defaults optional
│       ├── junit-platform.properties
│       ├── features/
│       │   ├── gateway/
│       │   │   └── health_and_routing.feature
│       │   ├── content/
│       │   │   ├── boards.feature
│       │   │   ├── hierarchy.feature
│       │   │   ├── quizzes.feature
│       │   │   ├── questions_contract.feature
│       │   │   └── freemium_download.feature
│       │   ├── user/
│       │   │   └── users_and_children.feature
│       │   ├── tv/
│       │   │   └── wifi_and_connected_tvs.feature
│       │   ├── attempts/
│       │   │   └── quiz_attempts.feature
│       │   ├── journeys/
│       │   │   ├── parent_child_tv_setup.feature
│       │   │   └── parent_initiated_quiz_session.feature
│       │   └── negative/
│       │       ├── validation.feature
│       │       └── not_found.feature
│       ├── schemas/
│       │   ├── question-response.json
│       │   └── quiz-download.json
│       └── test-data/
│           └── sample-question.json
```

### 5.1 Cucumber JUnit Platform runner (pattern)

```java
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value =
    "pretty,"
    + "summary,"
    + "html:build/reports/cucumber/cucumber.html,"
    + "json:build/reports/cucumber/cucumber.json,"
    + "junit:build/reports/cucumber/cucumber-junit.xml,"
    + "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.studyshield.regression")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @wip")
public class CucumberTest {
}
```

**Reports must be configured in the runner and/or `cucumber.properties` so every `./gradlew :ss-regression-suite:test` generates them without extra flags.**

Smoke-only runner:

```java
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@smoke and not @wip")
public class CucumberSmokeTest { }
```

---

## 6. Configuration

### 6.1 Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `GATEWAY_BASE_URL` | `http://localhost:8080` | **Only** base URL for suite |
| `SUITE_TIMEOUT_MS` | `30000` | Read timeout (Render cold start) |
| `SUITE_CONNECT_TIMEOUT_MS` | `10000` | Connect timeout |
| `SUITE_RETRY_MAX` | `5` | Retries on 502/503/connection reset |
| `SUITE_RETRY_BACKOFF_MS` | `2000` | Backoff between retries |
| `SUITE_CLEANUP` | `true` | Delete created resources after scenario/feature |
| `SUITE_PREFIX` | `reg_` | Prefix for unique names |
| `SUITE_AUTH_TOKEN` | empty | Future JWT |
| `CUCUMBER_FILTER_TAGS` | `not @wip` | Override tags from env/CI |
| `ALLURE_RESULTS_DIRECTORY` | `build/allure-results` | Allure raw results |

### 6.2 Cucumber tags (use Gherkin tags, not JUnit `@Tag` alone)

| Tag | Meaning | When to run |
|-----|---------|-------------|
| `@smoke` | Health + thin happy path per domain | Post-deploy / every staging smoke |
| `@regression` | Full CRUD + journeys + negatives | Nightly / pre-release |
| `@content` | Content service features | Content changes |
| `@user` | User/children | User service changes |
| `@attempts` | Quiz attempts | Attempts changes |
| `@tv` | WiFi / connected TVs | TV service changes |
| `@gateway` | Routing / health / unknown paths | Gateway changes |
| `@contract` | Mobile question/quiz JSON shape | Content + client contract |
| `@journeys` | Cross-service flows | Nightly / release |
| `@slow` | Large freemium seed (subjects × 5 × 10) | Nightly only |
| `@wip` | Incomplete — **always excluded** by default | Dev only |
| `@debug` | May hit direct ports — **never in CI default** | Local isolation |

```bash
# Examples
export CUCUMBER_FILTER_TAGS="@smoke and not @wip"
./gradlew :ss-regression-suite:test

export CUCUMBER_FILTER_TAGS="@regression and not @wip and not @slow"
./gradlew :ss-regression-suite:test

export CUCUMBER_FILTER_TAGS="@content and @contract"
./gradlew :ss-regression-suite:test
```

Gradle property bridge (recommended):

```bash
./gradlew :ss-regression-suite:test -Pcucumber.filter.tags="@smoke and not @wip"
```

---

## 7. Preconditions & environment setup

### 7.1 Local full stack

```bash
./gradlew :ss-content-service:bootRun   # 8081
./gradlew :ss-user-service:bootRun      # 8082
./gradlew :ss-quiz-attempts:bootRun     # 8083
./gradlew :ss-tv-device-service:bootRun  # 8084
./gradlew :ss-api-gateway:bootRun       # 8080

export GATEWAY_BASE_URL=http://localhost:8080
./gradlew :ss-regression-suite:test
# Reports under build/reports/cucumber/ and build/allure-results/
```

### 7.2 Staging (Render)

```bash
export GATEWAY_BASE_URL=https://<ss-api-gateway-on-render>
export SUITE_TIMEOUT_MS=60000
export SUITE_RETRY_MAX=8
export CUCUMBER_FILTER_TAGS="@smoke and not @wip"
./gradlew :ss-regression-suite:test
```

### 7.3 Health gate

**Hook `@BeforeAll` / first `@smoke` scenario:**

```
GET {GATEWAY_BASE_URL}/actuator/health
```

Retry until 200 or abort suite with clear message `ENV_NOT_READY`.

---

## 8. What to test (catalog)

Map each case to a **Scenario** (or Scenario Outline) in a `.feature` file.  
IDs below are stable references for reports and defect tracking (`@CT-QN01` optional tag).

### 8.1 Gateway

| ID | Scenario intent | Expect |
|----|-----------------|--------|
| GW-01 | Health UP | 200 |
| GW-02 | Unknown path | 404 (not 500) |
| GW-03 | GET boards via gateway | 200 |
| GW-04 | GET users via gateway | 200 |
| GW-05 | Quiz attempts by user | 200 / empty list |
| GW-06 | WiFi networks by user | 200 / empty list |
| GW-07 | Unsupported method on boards | 405 or 404, not 500 |
| GW-08 | Downstream failure messaging | 502/503 clear (chaos/manual) |

**Feature file:** `features/gateway/health_and_routing.feature`

### 8.2 Content (hierarchy + mobile contract)

Order: `Board → ClassGrade → Subject → ContentPack → Quiz → Question`

| ID | Area | Expect |
|----|------|--------|
| CT-B01–B07 | Board CRUD + 400/404 | standard REST |
| CT-G01–G04 | ClassGrade under board | 201/list/404 |
| CT-S01–S03 | Subjects incl. multi-subject | list by class |
| CT-P01–P03 | Content packs | by subject |
| CT-QZ01 | STANDARD quiz questionCount 10 | 201 |
| CT-QZ02 | SINGLE → questionCount 1 | 201 |
| CT-QZ03 | FREEMIUM + freemiumIndex 1..5 | 201 |
| CT-QZ04 | GET quiz by id includes questions | 200 |
| CT-QZ05 | List quizzes may omit question bodies | 200 |
| CT-QZ06 | Download pack with active questions | 200 |
| CT-QZ07 | 5 freemium quizzes per pack | download count |
| CT-QN01 | SINGLE_CHOICE options+correctAnswers | 201 |
| CT-QN02 | No legacy optionA–D fields | contract |
| CT-QN03 | TRUE_FALSE 2 options | 201 |
| CT-QN04 | TRUE_FALSE 4 options | 400 |
| CT-QN05 | FITB multi text answers | 201 |
| CT-QN06 | MULTIPLE_CHOICE multi ids | 201 |
| CT-QN07 | SINGLE_CHOICE two corrects | 400 |
| CT-QN08 | Choice type without options | 400 |
| CT-QN09 | resourceId/difficulty/languages/tags/points | round-trip |
| CT-QN10–11 | Blacklist excluded from active/download | filtered |
| CT-QN12 | orderIndex ordering | ascending |
| CT-QN13 | 10 questions on STANDARD quiz | count |

**Feature files:** `content/*.feature`, especially `questions_contract.feature`, `freemium_download.feature`

### 8.3 User

| ID | Scenario | Expect |
|----|----------|--------|
| US-01–US-04 | User CRUD | 201/200/204 |
| US-05–US-08 | Child under user | CRUD |
| US-09 | Delete user | documented cascade |
| US-10–11 | 400 / 404 | validation |

**Feature:** `user/users_and_children.feature`

### 8.4 TV device

| ID | Scenario | Expect |
|----|----------|--------|
| TV-01–02 | WiFi network create/list by user | 201/200 |
| TV-03–06 | Connected TV CRUD | 201/200/204 |
| TV-07 | Delete network | cascade policy |
| TV-08 | User isolation (if implemented) | no cross-user leak |

**Feature:** `tv/wifi_and_connected_tvs.feature`

### 8.5 Quiz attempts

| ID | Scenario | Expect |
|----|----------|--------|
| QA-01 | Start attempt | 201 |
| QA-02–03 | Submit / list answers | 201/200 |
| QA-04 | Complete attempt | score fields |
| QA-05–06 | List by user / child | includes attempt |
| QA-07 | Parent-initiated metadata | user present |
| QA-08–09 | Delete / 404 | as implemented |

**Feature:** `attempts/quiz_attempts.feature`  
*Confirm DTO field names from live `ss-quiz-attempts` controllers at implement time.*

### 8.6 Journeys (Gherkin features)

| ID | Feature | Tags |
|----|---------|------|
| J1 | Freemium bootstrap: subjects × 5 quizzes × 10 questions + download | `@regression @content @slow @contract` |
| J2 | Parent + child + WiFi + TV | `@regression @user @tv` |
| J3 | Download quiz → start attempt → answers → complete | `@regression @journeys @attempts` |
| J4 | Blacklist safety on active/download | `@regression @content @contract` |
| J5 | Multi-route smoke across all services | `@smoke @gateway` |

---

## 9. How to test (methodology with Cucumber)

### 9.1 Layering

```
.feature (what)
    → step definitions (glue)
        → API facades / fixtures (how)
            → GatewayClient (HTTP)
```

### 9.2 ScenarioContext (World)

Store at least:

- `lastResponse` (status, body, headers, elapsed ms)
- `createdIds` map (boards, quizzes, users, …) for cleanup
- `currentUserId`, `currentChildId`, `currentQuizId`, `currentAttemptId`
- `suitePrefix` unique per run

### 9.3 Hooks

| Hook | Responsibility |
|------|----------------|
| `@BeforeAll` | Load config; optional global health wait |
| `@Before` | Ensure context clean or inherit feature-scoped data; start timer |
| `@After` | On failure: attach request/response to **Cucumber + Allure**; cleanup scenario ids if `SUITE_CLEANUP=true` |
| `@AfterAll` | Best-effort purge remaining registry ids |

### 9.4 Step definition conventions

- `CommonHttpSteps`: status code, JSON path equals, array size, body contains key
- Domain steps call facades, not raw RestAssured scattered everywhere
- Use `@ParameterType` for enums (`QuestionType`, `ContentTier`) if helpful
- Doc strings for JSON bodies; DataTables for simple field maps

### 9.5 Retry policy

Retry only connection failures and **502/503/504**.  
Never retry expected **400/401/403/404** assertions.

### 9.6 Parallelism

- v1: **sequential** scenarios (shared free-tier DB)
- Future: Cucumber parallel with isolated prefixes per thread

### 9.7 Negative testing

Same matrix as before (missing fields, malformed JSON, unknown ids, wrong option shapes) expressed as Scenario Outlines:

```gherkin
Scenario Outline: Invalid question payloads are rejected
  When I create a question from file "<fixture>"
  Then the response status should be 400

  Examples:
    | fixture                        |
    | invalid/true_false_four_opts.json |
    | invalid/single_choice_two_answers.json |
```

---

## 10. Data factories

Same as product needs; callable from steps:

| Factory | Use in steps |
|---------|----------------|
| Board / ClassGrade / Subject / Pack | “a freemium content hierarchy exists…” |
| `standardQuiz(packId, freemiumIndex)` | STANDARD, 10, FREEMIUM |
| `singleChoiceQuestion(...)` | mobile-shaped options |
| `trueFalseQuestion` / `fitbQuestion` | type coverage |
| `user` / `child` | parent journeys |
| `wifi` / `tv` | TV setup |
| `attempt` | quiz session |

All display names: `SUITE_PREFIX + uuid fragment`.

---

## 11. Assertions (pass criteria)

### 11.1 HTTP

- Exact status
- JSON content-type when body expected
- Duration logged (soft assert optional max time)

### 11.2 Mobile question contract (mandatory step)

`Then the question response must match the mobile contract`:

- `questionType` ∈ SINGLE_CHOICE | MULTIPLE_CHOICE | TRUE_FALSE | FITB  
- `options` is array  
- `correctAnswers` non-empty array  
- **must not** include `optionA`, `optionB`, `optionC`, `optionD`, `correctOption`

### 11.3 Quiz download contract

- Quizzes include metadata (`contentTier`, `language`, `questionCount`)
- Detail/download includes `questions`
- Blacklisted excluded from active/download

---

## 12. Report generation (REQUIRED — always enabled)

Reports are a **first-class deliverable**. Every suite run must emit artifacts **without** the engineer remembering extra CLI flags.

### 12.1 Mandatory plugins (configure by default)

| Plugin | Output path | Purpose |
|--------|-------------|---------|
| `pretty` | console | Live progress |
| `summary` | console | Totals |
| `html` | `build/reports/cucumber/cucumber.html` | **Primary human report** |
| `json` | `build/reports/cucumber/cucumber.json` | CI tools, trends, re-import |
| `junit` / `junit:xml` | `build/reports/cucumber/cucumber-junit.xml` | CI test tab / gates |
| `io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm` | `build/allure-results/` | Rich step timeline, attachments |
| Gradle/JUnit platform | `build/test-results/test/*.xml` | Standard Gradle CI |

### 12.2 Allure (enabled)

- Dependency: `allure-cucumber7-jvm` + `allure-rest-assured` filter on GatewayClient
- On each failed scenario attach:
  - HTTP method + URL
  - Request body (truncate if huge)
  - Response status + body (truncate pack downloads; attach size + first quiz summary)
  - Scenario tags + `GATEWAY_BASE_URL` (no secrets)
- Generate HTML:

```bash
# after tests
allure generate build/allure-results -o build/reports/allure-report --clean
allure open build/reports/allure-report
```

CI should **always** publish:

1. `build/reports/cucumber/cucumber.html`
2. `build/reports/cucumber/cucumber.json`
3. `build/reports/cucumber/cucumber-junit.xml`
4. `build/allure-results/` (or generated Allure report directory)

### 12.3 `cucumber.properties` (example)

```properties
cucumber.publish.quiet=true
cucumber.plugin=pretty,summary,\
html:build/reports/cucumber/cucumber.html,\
json:build/reports/cucumber/cucumber.json,\
junit:build/reports/cucumber/cucumber-junit.xml,\
io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm
cucumber.glue=com.studyshield.regression
cucumber.features=classpath:features
cucumber.filter.tags=not @wip
```

*(Runner `@ConfigurationParameter` should match so plugins cannot be accidentally dropped.)*

### 12.4 Suite summary JSON (extra, recommended)

`build/reports/regression-summary.json`:

```json
{
  "suite": "ss-regression-suite",
  "framework": "cucumber-junit-platform",
  "gatewayBaseUrl": "http://localhost:8080",
  "startedAt": "2026-07-15T10:00:00Z",
  "finishedAt": "2026-07-15T10:05:00Z",
  "durationMs": 300000,
  "tagsFilter": "@smoke and not @wip",
  "totals": {
    "scenarios": 42,
    "passed": 40,
    "failed": 1,
    "skipped": 1
  },
  "reports": {
    "cucumberHtml": "build/reports/cucumber/cucumber.html",
    "cucumberJson": "build/reports/cucumber/cucumber.json",
    "cucumberJunit": "build/reports/cucumber/cucumber-junit.xml",
    "allureResults": "build/allure-results"
  },
  "failures": [
    {
      "name": "TRUE_FALSE with 4 options is rejected",
      "tags": ["@content", "@contract"],
      "feature": "questions_contract.feature",
      "message": "Expected 400 but was 201"
    }
  ],
  "exitCode": 1
}
```

Populate via Cucumber `Plugin` / `ConcurrentEventListener` or post-process `cucumber.json` in a Gradle task `regressionSummary`.

### 12.5 Gradle tasks

```bash
./gradlew :ss-regression-suite:test              # runs features + writes all reports
./gradlew :ss-regression-suite:allureReport      # optional dedicated task
./gradlew :ss-regression-suite:regressionSummary # optional JSON summary
```

`test` task must **not** disable reports. Set:

```gradle
tasks.named('test', Test) {
    useJUnitPlatform()
    systemProperty 'cucumber.filter.tags', System.getenv('CUCUMBER_FILTER_TAGS') ?: 'not @wip'
    systemProperty 'allure.results.directory', 'build/allure-results'
    // Ensure build/reports/cucumber exists
    reports {
        junitXml.required = true
        html.required = true
    }
}
```

### 12.6 CI publishing checklist

| Pipeline | Tags | Artifacts to upload |
|----------|------|---------------------|
| Post-deploy smoke | `@smoke and not @wip` | cucumber.html, junit xml, allure-results |
| Nightly | `@regression and not @wip` | full set + summary JSON |
| Release | `@regression and @contract` | full set; fail on any failure |

### 12.7 Pass/fail gates

- Cucumber non-zero exit if any scenario failed
- **Critical scenarios** (map to `@smoke` or explicit tags): GW-01, boards route, CT-QN01/02, CT-QZ06, user create, child create, wifi+tv create, attempt start+complete

### 12.8 What “reports enabled” means (acceptance)

Implementer is done on reporting only when:

1. A green or red run always creates **Cucumber HTML** openable in a browser  
2. **Cucumber JSON** exists for tooling  
3. **JUnit XML** exists for CI  
4. **Allure results** exist; failed steps show HTTP attachments  
5. README documents where to find each report  
6. No manual `-Dcucumber.plugin=...` required for the default set  

---

## 13. Flakiness control

| Cause | Mitigation |
|-------|------------|
| Render cold start | retries + long timeout in GatewayClient |
| Aiven idle | same |
| Shared staging | unique prefixes + cleanup hooks |
| Ambiguous steps | Cucumber strict mode (`cucumber.execution.strict=true` if available) / unique step text |
| Order dependency | Prefer independent scenarios; journeys explicit |

---

## 14. Cleanup strategy

1. `IdRegistry` records every created id.  
2. `@After` / `@AfterAll` deletes reverse order:

```
answers → attempts → questions → quizzes → packs → subjects → grades → boards
→ tvs → wifi networks → children → users
```

3. Optional `SUITE_CLEANUP_STRICT=true` fails suite if cleanup errors.

---

## 15. Implementation checklist (for coding LLM)

- [ ] Create `ss-regression-suite` Gradle module; `include` in `settings.gradle`
- [ ] Add Cucumber + JUnit Platform Suite + REST Assured + AssertJ + Allure
- [ ] Configure **default Cucumber plugins** for HTML, JSON, JUnit XML, Allure
- [ ] Implement `CucumberTest` / `CucumberSmokeTest` runners
- [ ] Implement `SuiteConfig`, `ScenarioContext`, `GatewayClient`
- [ ] Implement domain API facades + fixtures (mobile question JSON)
- [ ] Implement hooks (health, cleanup, Allure attachments)
- [ ] Write feature files for gateway smoke
- [ ] Write content hierarchy + **questions_contract.feature**
- [ ] Write user, tv, attempts features
- [ ] Write journey features J2/J3; J1 as `@slow`
- [ ] Wire `CUCUMBER_FILTER_TAGS` / Gradle `-P`
- [ ] Emit `regression-summary.json` (recommended)
- [ ] README: run commands + **report paths**
- [ ] Update `MEMORY.md` module table
- [ ] Optional `.github/workflows/regression.yml`
- [ ] Default profile **only** uses gateway `:8080`
- [ ] Local e2e run once with reports inspected

---

## 16. Sample smoke feature flow

`features/journeys/smoke_e2e.feature` tagged `@smoke`:

1. Gateway health  
2. Create board → grade → subject → pack → quiz → one SINGLE_CHOICE question  
3. GET quiz by id → questions ≥ 1  
4. Create user → child  
5. Create wifi → connected TV  
6. Start attempt → answer → complete  
7. Hooks cleanup  

Target: **&lt; 2 minutes** on warm local stack.

---

## 17. Mapping to other test layers

| Layer | Location | Role |
|-------|----------|------|
| Unit / MockMvc | each `ss-*/src/test` | Fast, H2, no gateway |
| **Cucumber API regression** | **`ss-regression-suite`** | Gherkin + real HTTP via gateway |
| Mobile UI | Interrupter Android | Out of scope |

---

## 18. Risks & open points

| Risk | Note |
|------|------|
| Auth not fully enforced | Steps prepared for `Authorization`; `@wip` until live |
| Attempt DTO drift | Read controllers at implement time |
| `/api/v1/parents/**`, `/quiz-results/**` | Probe; skip with `@wip` if missing |
| Large J1 data on free Aiven | Keep `@slow` out of smoke |
| Step definition sprawl | Enforce facade layer |

---

## 19. Success criteria

1. Module runs with `GATEWAY_BASE_URL` only.  
2. Features are Gherkin; glue is Java; HTTP via gateway.  
3. `@smoke` passes against local full stack.  
4. **Reports always generated:** Cucumber HTML + JSON + JUnit XML + Allure results.  
5. Contract scenarios prove mobile-shaped questions (not optionA–D).  
6. At least one cross-service journey feature passes.  
7. README lists how to run and where reports are.  

---

## 20. Quick reference — base paths (via gateway)

```
GET  /actuator/health

/api/v1/boards
/api/v1/class-grades
/api/v1/subjects
/api/v1/content-packs
/api/v1/quizzes
/api/v1/quizzes/{id}
/api/v1/quizzes/content-pack/{id}
/api/v1/quizzes/content-pack/{id}/download
/api/v1/questions
/api/v1/questions/quiz/{quizId}
/api/v1/questions/quiz/{quizId}/active

/api/v1/users
/api/v1/children
/api/v1/parents

/api/v1/quiz-attempts
/api/v1/quiz-attempts/{id}/complete
/api/v1/quiz-attempts/{id}/answers
/api/v1/quiz-results

/api/v1/wifi-networks
/api/v1/connected-tvs
```

---

## 21. Document history

| Version | Date | Notes |
|---------|------|--------|
| 1.0 | 2026-07-15 | Initial API regression spec (JUnit-centric) |
| 1.1 | 2026-07-15 | **Locked: Java Cucumber/Gherkin QA suite; reports always enabled** (HTML, JSON, JUnit XML, Allure) |

---

**End of component description.**  
Implement as Gradle module `ss-regression-suite` per this document; keep root `ss-regression-suite.md` as source of truth for the implementer LLM.
