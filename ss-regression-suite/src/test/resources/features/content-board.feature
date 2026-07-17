Feature: Board CRUD Operations
  As a content manager
  I want to manage boards
  So that I can organize educational content by board

  Scenario Outline: Create a board with valid data
    When I create a board with name "<name>" and code "<code>"
    Then the response status should be 201
    And the response should have an id field

    Examples:
      | name          | code    |
      | CBSE Board    | CBSE    |
      | ICSE Board    | ICSE    |
      | State Board   | STATE   |
      | IB Board      | IB      |

  Scenario: Retrieve all boards
    Given a board named "Test Board" exists
    When I get all boards
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: Delete a board
    Given a board named "Delete Board" exists
    When I delete the current board
    Then the response status should be 204
