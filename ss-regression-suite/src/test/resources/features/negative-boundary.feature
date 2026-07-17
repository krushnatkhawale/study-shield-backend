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

  Scenario: Create user with invalid email format
    When I POST "/api/v1/users" with body:
      """
      {"email": "not-an-email", "name": "Test", "phone": "123", "role": "PARENT", "active": true}
      """
    Then the response status should be 400 or 422
