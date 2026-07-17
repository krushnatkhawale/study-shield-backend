Feature: Connected TV CRUD Operations
  As a TV device manager
  I want to manage connected TVs
  So that I can track which TVs are connected to which networks

  Scenario: Create a connected TV on a WiFi network
    Given a parent user exists
    And a WiFi network "Living Room" exists for current user
    When I create a connected TV "Samsung TV"
    Then the response status should be 201
    And the response should have an id field

  Scenario: List connected TVs for a WiFi network
    Given a parent user exists
    And a WiFi network "Bedroom" exists for current user
    And a connected TV "LG TV" exists on current WiFi network
    When I get connected TVs for current WiFi network
    Then the response status should be 200
    And the response body should be a JSON array
