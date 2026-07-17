Feature: Quiz Attempt Lifecycle
  As a student
  I want to attempt quizzes
  So that I can test my knowledge and track progress

  Scenario: Start a quiz attempt
    Given a parent user exists
    And a child profile "Test Child" exists under current user
    And a board named "Attempt Board" exists
    And a class grade 10 exists under current board
    And a subject "Science" exists under current class grade
    And a content pack "Chemistry" exists under current subject
    And a STANDARD quiz exists in current pack
    When I start a quiz attempt for current user and child
    Then the response status should be 201
    And the response should have an id field

  Scenario: Submit an answer during a quiz
    Given a parent user exists
    And a child profile "Answer Child" exists under current user
    And a board named "Answer Board" exists
    And a class grade 10 exists under current board
    And a subject "Physics" exists under current class grade
    And a content pack "Optics" exists under current subject
    And a STANDARD quiz exists in current pack
    When I start a quiz attempt for current user and child
    Then the response status should be 201
    When I submit an answer for question 1
    Then the response status should be 201

  Scenario: Complete a quiz attempt
    Given a parent user exists
    And a child profile "Complete Child" exists under current user
    And a board named "Complete Board" exists
    And a class grade 10 exists under current board
    And a subject "Math" exists under current class grade
    And a content pack "Algebra" exists under current subject
    And a STANDARD quiz exists in current pack
    When I start a quiz attempt for current user and child
    Then the response status should be 201
    When I complete the quiz attempt with 8 correct answers
    Then the response status should be 200
    And the attempt result should include score fields

  Scenario: List attempts for a user
    Given a parent user exists
    When I get attempts for current user
    Then the response status should be 200
    And the response body should be a JSON array
