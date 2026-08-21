Feature: Negative Tests - Boundary Conditions
  As a regression suite
  I want to verify boundary condition handling
  So that the API handles edge cases correctly

  Scenario: Create board with very long name
    When I POST "/api/v1/boards" with body:
      """
      {"name": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", "code": "LONG", "description": "test", "active": true}
      """
    Then the response status should be 400 or 422
