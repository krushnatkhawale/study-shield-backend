@SMOKE
Feature: Quiz Attempts API Smoke Tests
  As a regression suite
  I want to verify quiz attempts service operations
  So that I know the quiz attempts service is functional

  Scenario: List attempts for a user
    Given a parent user exists
    When I get attempts for current user
    Then the response status should be 200
    And the response body should be a JSON array
