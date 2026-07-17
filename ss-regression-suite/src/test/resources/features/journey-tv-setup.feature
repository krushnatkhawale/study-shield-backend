Feature: Cross-Service Journey - TV Device
  As a parent
  I want to set up connected TVs for my child
  So that they can access educational content on the TV

  @JOURNEY
  Scenario: TV setup journey - create network and connect device
    # Step 1: Create parent
    When I create a parent user with name "TV Parent"
    Then the response status should be 201

    # Step 2: Create WiFi network
    When I create a WiFi network with SSID "Home WiFi"
    Then the response status should be 201

    # Step 3: Connect TV
    When I create a connected TV "Living Room TV"
    Then the response status should be 201

    # Step 4: Verify TV is connected
    When I get connected TVs for current WiFi network
    Then the response status should be 200
    And the response body should be a JSON array
