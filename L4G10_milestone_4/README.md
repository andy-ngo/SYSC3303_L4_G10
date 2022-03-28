# SYSC 3303 Project Iteration 4
# L4 Group 10

## Group Members:
 - Ali Fahd 101107270
 - Andy Ngo 101132278
 - Karim Mahrous 101150894
 - Scharara Islam 101149731

## How to Run:
To run the application, start with the Scheduler.java, then FloorSubsystem.java, and then ElevatorSubsystem.java concurrently for them to communicate with each other.


## Source Code:
ElevatorSubsystem.java
- A class that will check the floor request array and make sure if there are any request. With the request, the elevator will check to go up or down and to stop when it arrive. It will communicate with the scheduler whenever there has been arrival. This class is synchronized with the scheduler class.

Elevator.java
- A class that has all the elevator properties that can be used in tests cases and the elevatorSubsystem

ElevatorMotor.java
- A class to represent the function of the elevator to go up or down as well as stopping.

FloorRequest.java
- Data structure for floor requests which are imported.

FloorSubsystem.java
- A class that imports the floor requests and send them to the scheduler. Also updates arrival sensors.

Scheduler.java
- A class that act as a server to communicate with the Elevator class and the Floor Subsystem class.

ReadPropertyFile.java
- a class that reads the files for the program to run with.

## Test Included:
JUnit Test files to verify that it is working
- FloorSubsystemTest.java

## UML Diagrams:
- ClassDiagram4.png
- SequenceDiagram4.png
- Elevator_StateDiagram4.png
- Scheduler_StateDiagram4.png
- Elevator_FloorStuck_TimingDiagram4.png
- Elevator_doorStuck_TimingDiagram4.png

## Breakdown of Responsibilities:
## Iteration 4
Ali Fahd:
- Updated source codes

Karim Mahrous:
- Updated source codes

Andy Ngo:
- Updated source codes

Scharara Islam:
- Updated README
- Updated UML diagram
- Updated State Diagram
- Created Timing Diagram

## ITERATION 3
Ali Fahd:
- Updated source codes

Karim Mahrous:
- Updated source codes

Andy Ngo:
- Updated source codes

Scharara Islam:
- Updated README 
- Created Scheduler State diagram
- Updated UML Class Diagram

## Iteration 2
Ali Fahd:
- Updated source codes

Karim Mahrous:
- Designed the State Machine Sequences 
- Updated UML Class Diagram and Sequence Diagram

Andy Ngo:
- Updated source codes

Scharara Islam:
- Updated README 

## Iteration 1
Ali Fahd:
- Created the FLoorRequest, FloorSubsystem and Main
- Created the testing: FloorSubsystemTest and SchedulerTest

Andy Ngo:
- Created the Elevator and Elevator_Motor
- Created the testing: ElevatorTest

Karim Mahrous:
- UML of Class and Sequence DIagram

Scharara Islam:
- Created Scheduler class and edited by Ali Fahd
- README
