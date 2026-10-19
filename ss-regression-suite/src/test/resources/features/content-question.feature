Feature: Question CRUD Operations
  As a content manager
  I want to manage questions
  So that I can create quiz questions

  Scenario: Create and retrieve questions for a quiz
    Given a board named "Question Board" exists
    And a board class "Class 10" exists at ordinal 14 under current board
    And an offering for subject "Physics" exists under current board class
    And a content pack "Mechanics" exists under current offering
    And a STANDARD quiz exists in current pack
    When I create a question with text "What is Newton's first law?"
    Then the response status should be 201
    When I get questions for current quiz
    Then the response status should be 200
    And the response body should be a JSON array