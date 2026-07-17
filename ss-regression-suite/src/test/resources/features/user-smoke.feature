@SMOKE
Feature: User API Smoke Tests
  As a regression suite
  I want to verify user service CRUD operations
  So that I know the user service is functional

  Scenario: Create and retrieve a parent user
    When I create a parent user with name "Smoke User"
    Then the response status should be 201
    When I get user by id
    Then the response status should be 200
    And the response body should contain "Smoke User"

  Scenario: List users
    When I get all users
    Then the response status should be 200
    And the response body should be a JSON array
