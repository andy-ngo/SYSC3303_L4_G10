# SYSC 3303 Project Iteration 1 
# L4 Group 10

## Group Members:
 - Ali Fahd 101107270
 - Andy Ngo 101132278
 - Karim Mahrous
 - Scharara Islam 101149731

## How to Run:
To run the application, use Main.java

## Source Code:

Main.java
CLass that will create instances of each class and run the whole system.

Elevator.java
A class which allow to operate a function as an elevator in real life as well as synchronizing with the Scheduler System to get request.

Elevator_Moter.java
A class to represent the function of the elevator to go Up and Down as well as stopping.

FloorRequest.java
Data structure for floor requests whicih are imported.

FloorSubsystem.java
A class that imports the floor requests and send them to the scheduler. Also updates arrival sensors.

Scheduler.java
A class that act as a server to communicate with the Elevator class and the Floor Subsystem class.

## Breakdown of Responsibilities:
### Ali Fahd:
 - Created the FloorRequest, FloorSubsystem, and Main

### Andy Ngo:
- Created the Elevator and Elevator_Motor

### Karim Mahrous:
 - UML of Class and Sequence Diagram

### Scharara Islam 
- Created Scheduler class and edited by Ali Fahd and Andy Ngo
- README
