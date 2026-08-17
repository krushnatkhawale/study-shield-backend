Feature: Class Grade CRUD Operations
  As a content manager
  I want to manage class grades
  So that I can organize content by grade level

  Scenario Outline: Create a class grade under a board
    Given a board named "Grade Board" exists
    When I create a class grade "<className>" under current board
    Then the response status should be 201
    And the response should have an id field

    Examples:
      | className  |
      | Class 1    |
      | Class 5    |
      | Class 10   |
      | Class 12   |
      | Nursery    |
      | Junior KG  |
      | Senior KG  |
