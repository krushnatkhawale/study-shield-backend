Feature: Question CRUD Operations
  As a content manager
  I want to manage questions
  So that I can create quiz questions

  Scenario: Create and retrieve questions for a quiz
    Given a board named "Question Board" exists
    And a class grade 10 exists under current board
    And a subject "Physics" exists under current class grade
    And a content pack "Mechanics" exists under current subject
    And a STANDARD quiz exists in current pack
    When I create a question with text "What is Newton's first law?"
    Then the response status should be 201
    When I get questions for current quiz
    Then the response status should be 200
    And the response body should be a JSON array
