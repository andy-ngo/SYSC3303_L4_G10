/**
 * 
 */

/**
 * @author Ali Fahd
 *
 */
public class FloorRequest {
	private String requestTime;
    private int floorOrigin;
    private String direction;
    private int floorDestination;

    public FloorRequest(String requestTime, int floorOrigin, String direction, int floorDestination) {
		this.requestTime = requestTime;
		this.floorOrigin = floorOrigin;
		this.direction = direction;
		this.floorDestination = floorDestination;
	}

    /**
     * @return time stamp when elevator request was made
     */
    public String getRequestTime() {
        return this.requestTime;
    }
    
    /**
     * @return the floor where the request was made
     */
    public int getFloorOrigin() {
        return this.floorOrigin;
    }

    /**
     * @return returns direction of elevator request
     */
    public String getDirection() {
        return this.direction;
    }

    /**
     * @return the floor number where the destination is
     */
    public int getFloorDestination() {
        return this.floorDestination;
    }

    public void setFloorDestination(int floorNunmber) {
        this.floorDestination = floorNunmber;
    }
    
    public String toString() {
        String summary = "Elevator Request: Time - " + getRequestTime() + " - Floor Origin - " + getFloorOrigin() + " - Direction - " + getDirection() + " - Floor Destination - " + getFloorDestination();
        return summary;
    }
}
