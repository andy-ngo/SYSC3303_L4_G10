import java.net.InetAddress;
import java.util.*;


/**
 * Data structure that hold all the properties of the Elevator
 *
 * @author Nicolas Duciaume 10112471
 */
public class Elevator {
	String name = "";
	int port;
	private InetAddress address;
	private ArrayList<Integer> upQueue;
	private ArrayList<Integer> downQueue;
	private int currentFloor, currDestination;
	private ElevatorMotor direction;
	private ReadPropertyFile r;
	private boolean elevatorLampArray[];
	private int error;
	private String timestamp;
	private String status;

	private ArrayList<Integer> destinations;
	
	/**
	 * Initializes all variables
	 *
	 * @param name         Elevator's name
	 * @param port         Elevator's port
	 * @param address      Elevator's adress
	 * @param currentFloor current floor of elevator
	 */
	public Elevator(String name, int port, InetAddress address, int currentFloor) {
		this.name = name;
		this.port = port;
		this.address = address;
		upQueue = new ArrayList<>();
		downQueue = new ArrayList<>();
		destinations = new ArrayList<>();
		this.currentFloor = currentFloor;
		this.currDestination = -1;
		direction = ElevatorMotor.Stop;
		r = new ReadPropertyFile();
		this.error = 0;
		this.elevatorLampArray = new boolean[r.getNumFloors()];
		
		for (int i = 0; i < r.getNumFloors(); i++) {
			elevatorLampArray[i] = false;
		}
		this.timestamp = " ";
		this.status = "waiting";
	}

	public Elevator() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the current floor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * Returns the error code
	 *
	 * @return the error code
	 */
	public int getError() {
		return error;
	}

	/**
	 * Set the error
	 */
	public void setError(int x) {
		this.error = x;
	}

	/**
	 * Set the current floor
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

	/**
	 * @return the elevator's direction
	 */
	public ElevatorMotor getDirection() {
		return direction;
	}

	/**
	 * Set the direction
	 */
	public void setDirection(ElevatorMotor direction) {
		this.direction = direction;
	}

	/**
	 * remove first value from the up queue
	 */
	public void removeUp() {
		upQueue.remove(0);
	}

	/**
	 * remove 1st value from the down queue
	 */
	public void removeDown() {
		downQueue.remove(0);
	}

	/**
	 * @return name of elevator
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the down queue
	 */
	public ArrayList<Integer> getDownQueue() {
		return downQueue;
	}

	/**
	 * @return the up queue
	 */
	public ArrayList<Integer> getUpQueue() {
		return upQueue;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Set the port
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Add to the up queue queue
	 */
	public void addToUp(int floorNum) {
		upQueue.add(floorNum);
	}

	/**
	 * Add to the down queue
	 */
	public void addToDown(int floorNum) {
		downQueue.add(floorNum);
	}

	/**
	 * @return address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Set the address
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * @return timestamp
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @return status
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * Set the status
	 */
	public void setStatus(String status) {
		this.status = status;
		setDestination();
	}
	
	/**
	 * @return destination
	 */
	public int getDestination() {
		return this.currDestination;
	}

	/**
	 * 
	 * Set the status
	 */
	public void addDestination(int destination) {
		this.destinations.add(destination);
		if(destinations.size() == 1) {
			currDestination = destination;
		}
	}
	
	/**
	 * 
	 * Set the status
	 */
	private void setDestination() {
		if(this.status.contains("arr"))
			{
				destinations.remove(0);
				if(destinations.size() != 0) {
					currDestination = destinations.get(0);
				}else {
					currDestination = -1; 
				}
			}
	}
	
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * Sorts the arrays in the elevator
	 */
	public void sortArrays() {
		Collections.sort(this.upQueue);
		Collections.sort(this.downQueue);
		Collections.reverse(this.downQueue);
	}

	public void setElevatorLamps(boolean add, int floornum) {
		if (add) {
			this.elevatorLampArray[floornum - 1] = true;
		} else {
			this.elevatorLampArray[floornum - 1] = false;
		}

	}
}
