# SYSC 3303 Project Iteration 3
# L4 Group 10

## Group Members:
 - Ali Fahd 101107270
 - Andy Ngo 101132278
 - Karim Mahrous 101150894
 - Scharara Islam 101149731

## How to Run:
To run the application, use Main.java

## Source Code:

Main.java
- Class that will create instances of each class and run the whole system.

ElevatorSubsystem.java
- A class that will check the floor request array and make sure if there are any request. With the request, the elevator will check to go up or down and to stop when it arrive. It will communicate with the scheduler whenever there has been arrival. This class is synchronized with the scheduler class.
- A state machine was added into this iteration. Having the elevator call to the state machine method at each different state. 
  - The following states that were implemented are:
    - IDLE
    - OPERATE
    - UP
    - DOWN
    - STOP

Elevator_Motor.java
- A class to represent the function of the elevator to go up or down as well as stopping.

FloorRequest.java
- Data structure for floor requests which are imported.

FloorSubsystem.java
- A class that imports the floor requests and send them to the scheduler. Also updates arrival sensors.

Scheduler.java
- A class that act as a server to communicate with the Elevator class and the Floor Subsystem class.
- State Machine implemented and allow to control the scheduler by the given state
  - The SchedulerState: EmptyRequests, ReceivedRequests, EmptyArrivalSensors, ReceivedArrivalSensors

## Test Included:
JUnit Test files to verify that it is working
- ElevatorTest.java
- FloorSubsystemTest.java
- SchedulerTest.java

## UML Diagrams:
- ClassDiagram3.png
- SequenceDiagram3.png
- Elevator_StateDiagram3.png
- Scheduler_StateDiagram3.png

## Breakdown of Responsibilities:
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
