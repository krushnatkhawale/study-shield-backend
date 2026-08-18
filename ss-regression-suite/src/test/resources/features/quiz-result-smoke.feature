@SMOKE
Feature: Quiz Results API Smoke Tests
  As a regression suite
  I want to verify quiz results service operations
  So that I know the quiz results service is functional

  Scenario: Save a quiz result
    Given a parent user exists
    When I save a quiz result for child "Smoke Child" with score 8 and 10 total questions
    Then the response status should be 201
    And the quiz result response should contain resultId
    And the quiz result response message should be "Quiz result saved successfully"
    And the quiz result response should not contain errorCode

  Scenario: List all quiz results
    Given a parent user exists
    When I list all quiz results
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: List quiz results by child name
    Given a parent user exists
    When I save a quiz result for child "Filter Child" with score 6 and 10 total questions
    Then the response status should be 201
    When I list quiz results for child "Filter Child"
    Then the response status should be 200
    And the response body should be a JSON array
    And the quiz result list should contain child "Filter Child"
