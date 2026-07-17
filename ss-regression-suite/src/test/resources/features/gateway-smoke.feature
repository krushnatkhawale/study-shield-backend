@SMOKE
Feature: API Gateway Smoke Tests
  As a regression suite
  I want to verify the API Gateway is running and routing correctly
  So that I know the basic infrastructure is healthy

  Scenario: Gateway is running and responding
    When I GET "/actuator/health"
    Then the response status should be 200

  Scenario: Content service is reachable through gateway
    When I GET "/api/v1/boards"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: User service is reachable through gateway
    When I GET "/api/v1/users"
    Then the response status should be 200
    And the response body should be a JSON array
