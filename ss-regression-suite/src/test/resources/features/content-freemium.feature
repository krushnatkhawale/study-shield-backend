@SMOKE @CONTENT
Feature: Quiz bundle download (content API)
  As the mobile app
  I want quiz bundles via the gateway
  So that after a kid is added I can cache quizzes offline

  Scenario: Issue quiz bundle seeds catalog and returns quizzes with questions
    When I request a quiz bundle for class "1" with device id "smoke-device"
    Then the response status should be 200
    And the response body should be a JSON object
    And the response JSON path "packId" should be present
    And the response JSON path "quizzesPerSubject" should equal "5"
    And the response JSON path "quizzes" should be present
    And the response JSON path "quizzes[0].questions" should be present
    And the response JSON path "quizzes[0].questions[0].options" should be present
    And the response JSON path "quizzes[0].questions[0].correctAnswers" should be present

  Scenario: Quiz bundle can be re-fetched by id
    When I request a quiz bundle for class "1" with device id "smoke-device-get"
    Then the response status should be 200
    When I get quiz bundle by id
    Then the response status should be 200
    And the response JSON path "packId" should be present
    And the response JSON path "quizzes" should be present
