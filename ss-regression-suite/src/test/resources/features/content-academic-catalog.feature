Feature: Academic catalog (class levels and the offering matrix)
  As a content manager
  I want the global class-level spine and board-class offerings
  So that content is anchored to ordinals, not class-grade rows

  Scenario Outline: Class levels expose the global ordinal spine
    When I GET "/api/v1/class-levels/ordinal/<ordinal>"
    Then the response status should be 200
    And the response JSON path "ordinal" should equal "<ordinal>"

    Examples:
      | ordinal |
      | 2 |
      | 5 |
      | 12 |
      | 16 |
      | 17 |

  Scenario: All class levels come back as a list
    When I GET "/api/v1/class-levels"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario Outline: A board advertises a class via board-class
    Given a board named "Matrix Board" exists
    When I create a board class "<display>" at ordinal <ordinal> under current board
    Then the response status should be 201
    And the response should have an id field

    Examples:
      | display    | ordinal |
      | Nursery    | 2 |
      | Junior KG  | 3 |
      | Senior KG  | 4 |
      | Class 1    | 5 |
      | Class 5    | 9 |
      | Class 8    | 12 |
      | Class 10   | 14 |
      | Class 12   | 16 |

  Scenario: Board classes for a board can be listed
    Given a board named "Listing Board" exists
    And a board class "Class 6" exists at ordinal 10 under current board
    When I get board classes for current board
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: Seeded matrix - CBSE offers "Class 8", ENG offers "Year 4"
    When I load the board with code "CBSE"
    When I get board classes for current board
    Then the response status should be 200
    And the response body should be a JSON array
    And the response body should contain "Class 8"
    When I load the board with code "ENG"
    When I get board classes for current board
    Then the response status should be 200
    And the response body should contain "Year 4"

  Scenario: Offerings resolve across board labels (ENG "Year 4" -> ordinal 8)
    When I GET "/api/v1/offerings?boardCode=ENG&className=Year+4"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: Offerings are anchored to a (board class, global subject) pair
    Given a board named "Offering Board" exists
    And a board class "Class 8" exists at ordinal 12 under current board
    And an offering for subject "Math" exists under current board class
    When I get offerings for current board class
    Then the response status should be 200
    And the response body should be a JSON array
    And the response body should contain "MATH"