/**
 * Data Structure for State Machine for the Elevator Subsystem.
 * @author Ali Fahd
 *
 */
public class StateMachine {
	private int currentFloor;
    private Elevator_Motor motor;
    private boolean isIdle;

    /**
     * Constructor for class State Machine. Initializes each field.
     *
     */
    public StateMachine() {
		this.currentFloor = 0;
		this.motor = Elevator_Motor.Stop;
		this.isIdle = true;
	}

    /**
     * @return int currentFloor - current floor number state elevator is on
     */
    public int getCurrentFloor() {
        return currentFloor;
    }
    
    /**
     * @param int floorNumber - set the current floor to
     */
    public void setCurrentFloor(int floorNumber) {
        currentFloor = floorNumber;
    }

    /**
     * @return Elevator_Motor motor - get current movement of elevator
     */
    public Elevator_Motor getMotor() {
        return motor;
    }
    
    /**
     * @param Elevator_Motor motor - new movement of elevator
     */
    public void setMotor(Elevator_Motor motor) {
        this.motor = motor;
    }
    
    /**
     * @return int currentFloor - current floor number state elevator is on
     */
    public boolean getIsIdle() {
        return isIdle;
    }
    
    /**
     * @param boolean bool - set the isIsle state to the current boolean
     */
    public void setIsIdle(boolean bool) {
        this.isIdle = bool;
    }

    /**
     * @return String summary - the data of the state machine in a readable format.
     */
    public String toString() {
        return "State Machine: Current Floor - " + getCurrentFloor() + " - Motor Movement: - " + getMotor() + " - Is Idle: - " + getIsIdle();
    }
}
