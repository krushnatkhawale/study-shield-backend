@SMOKE @CONTENT
Feature: Freemium pack download (content API Option A)
  As the mobile app
  I want freemium packs via the gateway
  So that after a kid is added I can cache quizzes offline

  Scenario: Issue freemium pack seeds catalog and returns quizzes with questions
    When I request a freemium pack for class "1" with device id "smoke-device"
    Then the response status should be 200
    And the response body should be a JSON object
    And the response JSON path "packId" should be present
    And the response JSON path "freemiumQuizzesPerSubject" should equal "5"
    And the response JSON path "quizzes" should be present
    And the response JSON path "quizzes[0].questions" should be present
    And the response JSON path "quizzes[0].questions[0].options" should be present
    And the response JSON path "quizzes[0].questions[0].correctAnswers" should be present

  Scenario: Freemium pack can be re-fetched by id
    When I request a freemium pack for class "1" with device id "smoke-device-get"
    Then the response status should be 200
    When I get freemium pack by id
    Then the response status should be 200
    And the response JSON path "packId" should be present
    And the response JSON path "quizzes" should be present
