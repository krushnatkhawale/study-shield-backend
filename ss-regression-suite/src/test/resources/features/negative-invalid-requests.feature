Feature: Negative Tests - Invalid Requests
  As a regression suite
  I want to verify error handling for invalid requests
  So that the API returns appropriate error responses

  Scenario: Create board with missing required fields
    When I POST "/api/v1/boards" with body:
      """
      {"name": ""}
      """
    Then the response status should be 400

  Scenario: Get non-existent resource
    When I GET "/api/v1/boards/999999"
    Then the response status should be 404

  Scenario: Invalid HTTP method
    When I POST "/actuator/health" with body:
      """
      {}
      """
    Then the response status should be 405
