/**
 * 
 *
 * @author Ali FAhd
 */

public class Main {
    public static final String floor = "Floor Subsystem";
    public static final String elevator = "Elevator Subsystem";

    /**
     * Program enter method
     * @param args String[]
     */
    public static void main(String[] args) {
        Thread floorSystem, elevatorSystem;
        Scheduler scheduler = new Scheduler();

        floorSystem = new Thread(new FloorSubsytem(scheduler), floor);
        elevatorSystem = new Thread(new Elevator(scheduler), elevator);
        floorSystem.start();
        elevatorSystem.start();
    }
}