@MOBILE
Feature: Student Mobile API
  As a mobile client
  I want to manage students via /api/students
  So that the mobile app can register and list children

  Scenario: List students for current user
    When I GET "/api/students"
    Then the response status should be 200
    And the response body should be a JSON array

  Scenario: Create a student via mobile endpoint
    When I POST "/api/students" with body:
      """
      {"name": "Mobile Student", "gender": "MALE", "birthYear": 2015, "studentClass": "8th"}
      """
    Then the response status should be 201
    And the response should have an id field
    And the response JSON path "name" should equal "Mobile Student"

  Scenario: Update a student via mobile endpoint
    When I POST "/api/students" with body:
      """
      {"name": "Original Student", "gender": "FEMALE", "birthYear": 2016, "studentClass": "7th"}
      """
    Then the response status should be 201
    When I capture the response id
    When I update the captured student with body:
      """
      {"name": "Updated Student", "birthYear": 2016}
      """
    Then the response status should be 200
    And the response JSON path "name" should equal "Updated Student"

  Scenario: Delete a student via mobile endpoint
    When I POST "/api/students" with body:
      """
      {"name": "To Be Deleted", "gender": "MALE", "birthYear": 2014, "studentClass": "9th"}
      """
    Then the response status should be 201
    When I capture the response id
    When I DELETE the captured student
    Then the response status should be 204
