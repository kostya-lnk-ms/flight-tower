# Flight Control Tower

Create a program for a flight control tower which accepts events describing the
status of flights, the event data structure is as follows:

 - Plane ID
 - Plane Model
 - Origin
 - Destination
 - Event Type
 - Timestamp
 - Fuel Delta

Here are a series of example inputs:

  F222 747 DUBLIN LONDON Re-Fuel 2021-03-29T10:00:00 200  
  F551 747 PARIS LONDON Re-Fuel 2021-03-29T10:00:00 345  
  F324 313 LONDON NEW YORK Take-Off 2021-03-29T12:00:00 0  
  F123 747 LONDON CAIRO Re-Fuel 2021-03-29T10:00:00 428  
  F123 747 LONDON CAIRO Take-Off 2021-03-29T12:00:00 0  
  F551 747 PARIS LONDON Take-Off 2021-03-29T11:00:00 0  
  F551 747 PARIS LONDON Land 2021-03-29T12:00:00 -120  
  F123 747 LONDON CAIRO Land 2021-03-29T14:00:00 -324  

The control tower needs to know the following for each flight at any given point
in time:

 - Plane ID
 - Flight Status
 - Last known fuel level

For example at 2021-03-29T15:00:00, the output would be:

  F123 Landed 104
  F222 Awaiting-Takeoff 200 // KL: This is wrong as there is one record for F222 above, “Re-Fuel”
  F324 In-Flight 0 //KL: Wrong, one record - F324 Take-Off
  F551 Landed 225// KL: “Land” or “Landed” ?

The control tower may also need to issue corrections to those events based on
new information, this could be removing events or changing their data.

Given the update:

  F551 747 PARIS LONDON Land 2021-03-29T12:00:00 -300

The output would be:

  F123 Landed 104  
  F222 Awaiting-Takeoff 200  
  F324 In-Flight 0  
  F551 Landed 45  

Or given the removal of an event:

  F551 747 PARIS LONDON Land 2021-03-29T12:00:00 -120

The output would be:

  F123 Landed 104  
  F222 Awaiting-Takeoff 200  
  F324 In-Flight 0  
  F551 In-Flight 345 // KL: Why Inf-Flight, it should be reverted to Take-Off  

Interacting with the program via the command line or a repl is sufficient for
this task.

Please add an appropriate test suite

KL Update:
The original field delimiters cause ambigious parsing, hence CSV is to be used.

# Assumptions
Some corrections to the original specification have been made, which is included into the solution.

The specification.docx contains the specification along with some comments added.
Namely, the test data suggests that there should be some "automatic" plane state management
assumed e.g. if the time is past some known event (like Take-Off), the plane is assumed being “in-flight” later on by the sample data.

This kind of behaviour is not described by the specification, hence it is not implemented.
The above means that all the plane statuses reported are the last known ones.

There are some improvements, which could be made, like:
1) Caching of property values inside the temporal history
2) More rigorous testing 
Current solution is done to fit the task into "couple of hours" as per specification.

# How to build this project
Requires maven
```
mvn install
```
# How to run the demo app
```
mvn exec:java
```
# Another way to check functionality
The project comes with cucumber-based acceptance tests. 

These can be easily mended to run more test cases
