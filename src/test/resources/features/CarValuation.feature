Feature: Automated Car Valuation Validation

  Scenario: Validate car valuation details from an external website
    Given the following input files for processing:
      | input_file       | output_file      |
      | car_input - V6.txt    | car_output - V6.txt  |
    When I extract registration numbers from "car_input - V6.txt" using predefined patterns
    And I perform a car valuation search for each registration on "car-analytics"