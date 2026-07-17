Feature: Cross-Service Journey
  As a parent with a child
  I want to complete the full journey from registration to quiz attempt
  So that I can use the complete StudyShield platform

  @JOURNEY
  Scenario: Full parent journey - register, setup child, take quiz
    # Step 1: Create parent
    When I create a parent user with name "Journey Parent"
    Then the response status should be 201

    # Step 2: Create child
    When I create a child profile "Journey Child"
    Then the response status should be 201

    # Step 3: Create content hierarchy
    Given a board named "Journey Board" exists
    And a class grade 8 exists under current board
    And a subject "Math" exists under current class grade
    And a content pack "Algebra" exists under current subject
    And a STANDARD quiz exists in current pack

    # Step 4: Start quiz attempt
    When I start a quiz attempt for current user and child
    Then the response status should be 201

    # Step 5: Submit answers
    When I submit an answer for question 1
    Then the response status should be 201
    When I submit an answer for question 2
    Then the response status should be 201

    # Step 6: Complete quiz
    When I complete the quiz attempt with 2 correct answers
    Then the response status should be 200
    And the attempt result should include score fields

    # Step 7: Verify attempts
    When I get attempts for current user
    Then the response status should be 200
