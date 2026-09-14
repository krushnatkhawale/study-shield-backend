Feature: Quiz CRUD Operations
  As a content manager
  I want to manage quizzes
  So that I can create assessments for students

  Scenario: Create a quiz in a content pack
    Given a board named "Quiz Board" exists
    And a board class "Class 10" exists at ordinal 14 under current board
    And an offering for subject "Math" exists under current board class
    And a content pack "Electronics" exists under current offering
    When I create a quiz with name "Unit 1 Quiz" and code "U1Q"
    Then the response status should be 201