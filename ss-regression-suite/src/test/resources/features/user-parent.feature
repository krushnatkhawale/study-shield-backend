Feature: Parent User CRUD Operations
  As a system administrator
  I want to manage parent users
  So that parents can register and access the platform

  Scenario Outline: Create a parent user with valid data
    When I create a parent user with name "<name>"
    Then the response status should be 201
    And the response should have an id field

    Examples:
      | name              |
      | John Smith        |
      | Maria Garcia      |
      | Priya Patel       |

  Scenario: Retrieve a parent user by id
    Given a parent user exists
    When I get user by id
    Then the response status should be 200
    And the response body should contain "parent@test.com"

  Scenario: List all users
    When I get all users
    Then the response status should be 200
    And the response body should be a JSON array
