@SMOKE
Feature: Content API Smoke Tests
  As a regression suite
  I want to verify content service CRUD operations
  So that I know the content service is functional

  Scenario: Create and retrieve a board
    When I create a board with name "Smoke Board" and code "SB"
    Then the response status should be 201
    When I get board by id
    Then the response status should be 200
    And the response body should contain "Smoke Board"

  Scenario: List boards
    When I get all boards
    Then the response status should be 200
    And the response body should be a JSON array
