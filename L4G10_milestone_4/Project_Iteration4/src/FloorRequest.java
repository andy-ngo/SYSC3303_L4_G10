

import java.sql.Timestamp;

public class FloorRequest {

    private String requestTime;
    private long travelTime;
    private long doorTime;
    private int floorRequestOrigin;
    private int floorDestination;
    private Direction direction;

    /**
     * Initializes all the variables, when parameter are given
     *
     * @param requestTime      Current real time of the elevator
     * @param travelTime       Time it takes for the elevator to run travel between one floor to the other
     * @param doorTime         Time it takes for the door to open/close
     * @param floorOrigin      The original floor the elevator was called on
     * @param floorDestination The destination floor
     * @param direction        The direction the elevator need to go
     */
    public FloorRequest(String requestTime, long travelTime, long doorTime, int floorOrigin, int floorDestination,
                        Direction direction) {
        this.requestTime = requestTime;
        this.direction = direction;
        this.floorDestination = floorDestination;
        this.floorRequestOrigin = floorOrigin;
        this.travelTime = travelTime;
        this.doorTime = doorTime;
    }

    /**
     * A default constructor for the elevator, when there is no elevator request, but the elevator is stopped.
     */
    public FloorRequest() {
        this.requestTime = (String.valueOf(new Timestamp(System.currentTimeMillis())));
        this.direction = Direction.STOPPED;
        this.floorDestination = -1;
        this.floorRequestOrigin = -1;
        this.travelTime = -1L;
        this.doorTime = -1L;
    }

    /**
     * @return the time stamp
     */
    public String getRequestTime() {
        return this.requestTime;
    }

    public void setFloorDestination(int x) {
        this.floorDestination = x;
    }

    /**
     * @return travel time of this elevator
     */
    public long getTravelTime() {
        return this.travelTime;
    }

    /**
     * @return the original floor
     */
    public int getFloorRequestOrigin() {
        return this.floorRequestOrigin;
    }

    /**
     * @return returns directions
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * @return the destination floor
     */
    public int getFloorDestination() {
        return this.floorDestination;
    }

    /**
     * @return the door close/open time
     */
    public long getDoorTime() {
        return this.doorTime;
    }

    public String toString() {
        String stringVersion = getRequestTime() + " " + getFloorRequestOrigin() + " " + getDirection() + " " + getFloorDestination();
        return stringVersion;
    }

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//
//		FloorRequest f = (FloorRequest) obj;
//
//		return (f.requestTime == this.getRequestTime() &&
//				f.travelTime == this.travelTime &&
//				f.doorTime == this.doorTime &&
//				f.floorRequestOrigin == this.floorRequestOrigin &&
//				f.floorDestination == this.floorDestination &&
//				f.direction == this.direction);
//	}
}