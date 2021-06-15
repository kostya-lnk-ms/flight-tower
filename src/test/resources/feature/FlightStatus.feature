Feature: Flight tower

  Scenario: Add flight status and render current status
    Given Empty event history
    When The following event added
      | planeID | planeModel | origin | destination | eventType | timestamp           | fuelDelta |
      | F222    | 747        | DUBLIN | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 200       |
      | F551    | 747        | PARIS  | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 345       |
      | F324    | 313        | LONDON | NEW YORK    | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F123    | 747        | LONDON | CAIRO       | Re-Fuel   | 2021-03-29T10:00:00 | 428       |
      | F123    | 747        | LONDON | CAIRO       | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Take-Off  | 2021-03-29T11:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Landed    | 2021-03-29T12:00:00 | -120      |
      | F123    | 747        | LONDON | CAIRO       | Landed    | 2021-03-29T14:00:00 | -324      |
    Then Flight status at "2021-03-29T15:00:00" should be
      | F123 Landed 104  |
      | F222 Re-Fuel 200 |
      | F324 Take-Off 0  |
      | F551 Landed 225  |

  Scenario: Add flight status, issue some corrections and render current status
    Given Empty event history
    And The following event added
      | planeID | planeModel | origin | destination | eventType | timestamp           | fuelDelta |
      | F222    | 747        | DUBLIN | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 200       |
      | F551    | 747        | PARIS  | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 345       |
      | F324    | 313        | LONDON | NEW YORK    | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F123    | 747        | LONDON | CAIRO       | Re-Fuel   | 2021-03-29T10:00:00 | 428       |
      | F123    | 747        | LONDON | CAIRO       | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Take-Off  | 2021-03-29T11:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Landed    | 2021-03-29T12:00:00 | -120      |
      | F123    | 747        | LONDON | CAIRO       | Landed    | 2021-03-29T14:00:00 | -324      |
    When The following event added
      | planeID | planeModel | origin | destination | eventType | timestamp           | fuelDelta |
      | F551    | 747        | PARIS  | LONDON      | Landed    | 2021-03-29T12:00:00 | -300      |
    Then Flight status at "2021-03-29T15:00:00" should be
      | F123 Landed 104  |
      | F222 Re-Fuel 200 |
      | F324 Take-Off 0  |
      | F551 Landed 45   |

  Scenario: Add flight status, render current status, issue some corrections and re-render
    Given Empty event history
    And The following event added
      | planeID | planeModel | origin | destination | eventType | timestamp           | fuelDelta |
      | F222    | 747        | DUBLIN | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 200       |
      | F551    | 747        | PARIS  | LONDON      | Re-Fuel   | 2021-03-29T10:00:00 | 345       |
      | F324    | 313        | LONDON | NEW YORK    | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F123    | 747        | LONDON | CAIRO       | Re-Fuel   | 2021-03-29T10:00:00 | 428       |
      | F123    | 747        | LONDON | CAIRO       | Take-Off  | 2021-03-29T12:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Take-Off  | 2021-03-29T11:00:00 | 0         |
      | F551    | 747        | PARIS  | LONDON      | Landed    | 2021-03-29T12:00:00 | -120      |
      | F123    | 747        | LONDON | CAIRO       | Landed    | 2021-03-29T14:00:00 | -324      |
    When The following event has been removed
      | planeID | planeModel | origin | destination | eventType | timestamp           | fuelDelta |
      | F551    | 747        | PARIS  | LONDON      | Landed    | 2021-03-29T12:00:00 | -120      |
    Then Flight status at "2021-03-29T15:00:00" should be
      | F123 Landed 104   |
      | F222 Re-Fuel 200  |
      | F324 Take-Off 0   |
      | F551 Take-Off 345 |
