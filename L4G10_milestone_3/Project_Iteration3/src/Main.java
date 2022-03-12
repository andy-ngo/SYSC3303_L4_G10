/**
 * Class used to run the whole elevator system.
 *
 * @author Ali Fahd
 */

public class Main {
    public static final String floor = "Floor Subsystem";
    public static final String elevator = "Elevator Subsystem";

    /**
     * Creates instances of the classes needed to run the elevator system. Runs the threads.
     * @param args String[]
     */
    public static void main(String[] args) {
        Thread floorSystem, elevatorSystem1, elevatorSystem2, elevatorSystem3, elevatorSystem4;	// threads for floor subsystem and elevator system 
        Scheduler scheduler = new Scheduler();// initialize scheduler
        ElevatorSubsystem elevator1 = new ElevatorSubsystem(scheduler, 1, 1);
        ElevatorSubsystem elevator2 = new ElevatorSubsystem(scheduler, 2, 1);
        ElevatorSubsystem elevator3 = new ElevatorSubsystem(scheduler, 3, 1);
        ElevatorSubsystem elevator4 = new ElevatorSubsystem(scheduler, 4, 1);

        floorSystem = new Thread(new FloorSubsytem(scheduler), floor);
        elevatorSystem1 = new Thread(elevator1, elevator);
        elevatorSystem2 = new Thread(elevator2, elevator);
        elevatorSystem3 = new Thread(elevator3, elevator);
        elevatorSystem4 = new Thread(elevator4, elevator);
        scheduler.addElevator(elevator1);
        scheduler.addElevator(elevator2);
        scheduler.addElevator(elevator3);
        scheduler.addElevator(elevator4);
        //run threads
        floorSystem.start();
        elevatorSystem1.start();
    }
}