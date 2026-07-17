Feature: Subject CRUD Operations
  As a content manager
  I want to manage subjects
  So that I can organize content by subject

  Scenario: Create a subject under a class grade
    Given a board named "Subject Board" exists
    And a class grade 10 exists under current board
    When I create a board with name "Math Subject" and code "MATH"
    Then the response status should be 201
