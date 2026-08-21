Feature: Question bank filtered by class/age
  As a parent,
  I want the quiz questions to match my child's class/age,
  so the practice feels relevant.

  Background:
    Given the question bank is seeded

  Scenario: Sr KG child gets age-appropriate questions, not placeholders
    When I request a quiz bundle for class "Sr KG" with device id "qb-srkg-device"
    Then the response status should be 200 or 201
    And every question in the bundle is a real curated question

  Scenario: Class 1 child gets age-appropriate questions, not placeholders
    When I request a quiz bundle for class "Class 1" with device id "qb-class1-device"
    Then the response status should be 200 or 201
    And every question in the bundle is a real curated question

  Scenario: A session never starts empty for an unseeded class
    When I request a quiz bundle for class "Class 5" with device id "qb-fallback-device"
    Then the response status should be 200 or 201
    And every question in the bundle is a real curated question

  Scenario: Age alone can pick the right class band
    When I request a quiz bundle for a child aged 5 with device id "qb-age5-device"
    Then the response status should be 200 or 201
    And every question in the bundle is a real curated question
