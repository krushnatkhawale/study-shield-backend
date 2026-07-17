Feature: Child Profile CRUD Operations
  As a parent
  I want to manage my child's profile
  So that my child can access personalized content

  Scenario: Create a child profile under a parent
    Given a parent user exists
    When I create a child profile "Little Star"
    Then the response status should be 201
    And the response should have an id field

  Scenario: List children for a parent
    Given a parent user exists
    And a child profile "Child One" exists under current user
    When I get children for current user
    Then the response status should be 200
    And the response body should be a JSON array
