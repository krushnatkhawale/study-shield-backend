Feature: Quiz Results Lifecycle
  As a mobile app user
  I want to save and retrieve quiz results
  So that my child's quiz performance is tracked and visible across sessions

  Scenario: Save a quiz result with all fields
    Given a parent user exists
    When I save a quiz result for child "Alice" with score 8, 10 total questions, and content "Algebra Quiz"
    Then the response status should be 201
    And the quiz result response should contain resultId
    And the quiz result response should not contain errorCode

  Scenario: Save a quiz result with minimal fields
    Given a parent user exists
    When I save a quiz result for child "Bob" with score 5 and 10 total questions
    Then the response status should be 201
    And the quiz result response should contain resultId

  Scenario: List all saved results
    Given a parent user exists
    When I save a quiz result for child "Charlie" with score 9, 10 total questions, and content "Science Quiz"
    Then the response status should be 201
    When I list all quiz results
    Then the response status should be 200
    And the response body should be a JSON array
    And the quiz result list should contain at least 1 result(s)
    And the quiz result list item should have expected fields

  Scenario: Filter results by child name
    Given a parent user exists
    When I save a quiz result for child "Diana" with score 7, 10 total questions, and content "History Quiz"
    Then the response status should be 201
    When I list quiz results for child "Diana"
    Then the response status should be 200
    And the response body should be a JSON array
    And the quiz result list should contain child "Diana"

  Scenario: Reject quiz result with missing child name
    Given a parent user exists
    When I save a quiz result with missing child name
    Then the response status should be 400

  Scenario: Reject quiz result with negative score
    Given a parent user exists
    When I save a quiz result with negative score
    Then the response status should be 400
