@SMOKE
Feature: TV Device API Smoke Tests
  As a regression suite
  I want to verify TV device service CRUD operations
  So that I know the TV device service is functional

  Scenario: Create and list WiFi networks
    Given a parent user exists
    When I create a WiFi network with SSID "Smoke Network"
    Then the response status should be 201
    When I get WiFi networks for current user
    Then the response status should be 200
    And the response body should be a JSON array
