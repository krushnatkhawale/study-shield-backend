Feature: Subject CRUD Operations
  As a content manager
  I want to manage the global subject catalog
  So that offerings can reference any subject once

  Scenario: Create a global subject
    When I create a subject with name "Environmental Studies"
    Then the response status should be 201
    And the response should have an id field

  Scenario: List all subjects
    Given a global subject "Earth Science" exists
    When I get all subjects
    Then the response status should be 200
    And the response body should be a JSON array