Feature: Quiz CRUD Operations
  As a content manager
  I want to manage quizzes
  So that I can create assessments for students

  Scenario: Create a quiz in a content pack
    Given a board named "Quiz Board" exists
    And a class grade 10 exists under current board
    And a subject "Science" exists under current class grade
    And a content pack "Electronics" exists under current subject
    When I create a quiz with name "Unit 1 Quiz" and code "U1Q"
    Then the response status should be 201
