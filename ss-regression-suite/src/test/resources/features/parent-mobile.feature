@MOBILE
Feature: Parent Mobile API
  As a mobile client
  I want to manage parents via /api/parents
  So that the mobile app can register and list parents

  Scenario: List parents
    When I GET "/api/parents"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: Create a parent via mobile endpoint
    When I POST "/api/parents" with body:
      """
      {"name": "Mobile Parent", "gender": "FEMALE", "relation": "MOTHER", "type": "PARENT"}
      """
    Then the response status should be 201
    And the response should have an id field
    And the response JSON path "name" should equal "Mobile Parent"
    And the response JSON path "type" should equal "ACCOUNT_HOLDER"

  Scenario: Update current parent via /api/parents/me
    When I POST "/api/parents" with body:
      """
      {"name": "Original Name", "gender": "MALE", "relation": "FATHER", "type": "PARENT"}
      """
    Then the response status should be 201
    When I PUT "/api/parents/me" with body:
      """
      {"name": "Updated Name"}
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Updated Name"

  Scenario: Delete a parent
    When I POST "/api/parents" with body:
      """
      {"name": "To Be Deleted", "gender": "MALE", "relation": "FATHER", "type": "PARENT"}
      """
    Then the response status should be 201
    When I capture the response id
    When I DELETE the captured parent
    Then the response status should be 204
