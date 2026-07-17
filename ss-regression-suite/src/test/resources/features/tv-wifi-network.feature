Feature: WiFi Network CRUD Operations
  As a TV device manager
  I want to manage WiFi networks
  So that I can associate connected TVs with networks

  Scenario: Create a WiFi network
    Given a parent user exists
    When I create a WiFi network with SSID "Home Network"
    Then the response status should be 201
    And the response should have an id field

  Scenario: List WiFi networks for a user
    Given a parent user exists
    And a WiFi network "Test Network" exists for current user
    When I get WiFi networks for current user
    Then the response status should be 200
    And the response body should be a JSON array
