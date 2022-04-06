import java.sql.Timestamp;

public class FloorRequest {

    private String requestTime;
    private int floorRequestOrigin;
    private ElevatorMotor direction;
    private int floorDestination;
    private long travelTime;
    private long doorTime;

    /**
     * Constructor for floor request class
     *
     * @param requestTime Current real time of the elevator
     * @param floorOrigin The original floor the elevator was called on
     * @param direction The direction the elevator need to go
     * @param floorDestination The destination floor
     * @param travelTime Time it takes for the elevator to run travel between one floor to the other
     * @param doorTime Time it takes for the door to open/close
     */
    public FloorRequest(String requestTime, int floorOrigin, ElevatorMotor direction, int floorDestination, long travelTime, long doorTime) {
        this.requestTime = requestTime;
        this.floorDestination = floorDestination;
        this.direction = direction;
        this.floorRequestOrigin = floorOrigin;
        this.travelTime = travelTime;
        this.doorTime = doorTime;
    }

    /**
     * A default constructor for the elevator, when there is no elevator request, but the elevator is stopped.
     */
    public FloorRequest() {
        this.requestTime = (String.valueOf(new Timestamp(System.currentTimeMillis())));
        this.direction = ElevatorMotor.STOP;
        this.floorDestination = -1;
        this.floorRequestOrigin = -1;
        this.travelTime = -1L;
        this.doorTime = -1L;
    }

    /**
     * @return String the time stamp
     */
    public String getRequestTime() {
        return this.requestTime;
    }

    /**
     * @param int floor num
     */
    public void setFloorDestination(int num) {
        this.floorDestination = num;
    }

    /**
     * @return long travel time of this elevator
     */
    public long getTravelTime() {
        return this.travelTime;
    }

    /**
     * @return int the original floor
     */
    public int getFloorRequestOrigin() {
        return this.floorRequestOrigin;
    }

    /**
     * @return ElevatorMotor direction
     */
    public ElevatorMotor getElevatorMotor() {
        return this.direction;
    }

    /**
     * @return int the destination floor
     */
    public int getFloorDestination() {
        return this.floorDestination;
    }

    /**
     * @return long the door time
     */
    public long getDoorTime() {
        return this.doorTime;
    }

    /**
     * @return string summary of floor request
     */
    public String toString() {
        String stringVersion = getRequestTime() + " " + getFloorRequestOrigin() + " " + getElevatorMotor() + " " + getFloorDestination();
        return stringVersion;
    }

}