Feature: API Contract Validation
  As a regression suite
  I want to verify API responses match expected contracts
  So that consumers can rely on consistent response formats

  Scenario: Board list response has expected structure
    When I GET "/api/v1/boards"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: User list response has expected structure
    When I GET "/api/v1/users"
    Then the response status should be 200
    And the response body should be a JSON array
