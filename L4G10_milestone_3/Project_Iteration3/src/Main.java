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
        Thread floorSystem, elevatorSystem;	// threads for floor subsystem and elevator system 
        Scheduler scheduler = new Scheduler();// initialize scheduler

        floorSystem = new Thread(new FloorSubsytem(scheduler), floor);
        elevatorSystem = new Thread(new ElevatorSubsystem(scheduler), elevator);
        //run threads
        floorSystem.start();
        elevatorSystem.start();
    }
}