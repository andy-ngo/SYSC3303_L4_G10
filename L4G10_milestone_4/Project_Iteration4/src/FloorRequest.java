import java.sql.Timestamp;

/**
 * Data Structure for Floor Requests that are inputed form the data file.
 * @author Ali Fahd
 *
 */
public class FloorRequest {
	private String requestTime;
    private int floorOrigin;
    private ElevatorMotor direction;
    private int floorDestination;
    private long travelTime;
    private long doorTime;

    /**
     * Constructor for class Floor Request. Initializes each field.
     * @param String request time - time when request was made
     * @param int floor origin - floor where user got on
     * @param String request time - time when request was made
     * @param int floor destination - floor where user wants to get off
     *
     */
    public FloorRequest(String requestTime, int floorOrigin, ElevatorMotor direction, int floorDestination, long travelTime, long doorTime) {
		this.requestTime = requestTime;
		this.floorOrigin = floorOrigin;
		this.direction = direction;
		this.floorDestination = floorDestination;
        this.travelTime = travelTime;
        this.doorTime = doorTime;
	}
    
    /**
     * Default constructor
     */
    public FloorRequest() {
        this.requestTime = (String.valueOf(new Timestamp(System.currentTimeMillis())));
        this.direction = ElevatorMotor.Stop;
        this.floorDestination = -1;
        this.floorOrigin = -1;
        this.travelTime = -1L;
        this.doorTime = -1L;
    }

    /**
     * @return String requestTime - time stamp when elevator request was made
     */
    public String getRequestTime() {
        return requestTime;
    }
    
    /**
     * @return int floorOrigin - the floor where the request was made
     */
    public int getFloorOrigin() {
        return floorOrigin;
    }

    /**
     * @return String direction - returns direction of elevator request
     */
    public String getDirection() {
        return direction.toString();
    }

    /**
     * @return int floorDestination - the floor number where the destination is
     */
    public int getFloorDestination() {
        return this.floorDestination;
    }

    /**
     * Sets the floor destination field.
     * 
     * @param int floorNumber - the floor number to set the floor destination to
     */
    public void setFloorDestination(int floorNunmber) {
        this.floorDestination = floorNunmber;
    }

    /**
     * @return String summary - the data of the request in a readable format.
     */
    public String toString() {
        String stringVersion = getRequestTime() + " " + getFloorOrigin() + " " + getDirection() + " " + getFloorDestination();
        return stringVersion;
    }
}
