import java.net.InetAddress;
import java.util.*;

public class Elevator {
	String id = "";
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
	 * Constructor for elevator.
	 *
	 * @param id elevator's id
	 * @param port elevator's port
	 * @param address elevator's address
	 * @param currentFloor current floor of elevator
	 */
	public Elevator(String id, int port, InetAddress address, int currentFloor) {
		this.id = id;
		this.port = port;
		this.address = address;
		upQueue = new ArrayList<>();
		downQueue = new ArrayList<>();
		destinations = new ArrayList<>();
		this.currentFloor = currentFloor;
		this.currDestination = -1;
		direction = ElevatorMotor.STOP;
		r = new ReadPropertyFile();
		this.error = 0;
		this.elevatorLampArray = new boolean[r.getNumFloors()];
		
		for (int i = 0; i < r.getNumFloors(); i++) {
			elevatorLampArray[i] = false;
		}
		this.timestamp = " ";
		this.status = "waiting";
	}

	public Elevator() {}

	/**
	 * @return int the current floor
	 */
	public int getCurrentFloor() {
		return currentFloor;
	}

	/**
	 * @return int the error code
	 */
	public int getError() {
		return error;
	}

	/**
	 * Set error
	 */
	public void setError(int error) {
		this.error = error;
	}

	/**
	 * Set current floor
	 */
	public void setCurrentFloor(int currentFloor) {
		this.currentFloor = currentFloor;
	}

	/**
	 * @return ElevatorMotor the elevator's direction
	 */
	public ElevatorMotor getElevatorMotor() {
		return direction;
	}

	/**
	 * Set the elevator direction
	 */
	public void setElevatorMotor(ElevatorMotor motor) {
		this.direction = motor;
	}

	/**
	 * remove first value from the up queue
	 */
	public void removeUp() {
		upQueue.remove(0);
	}

	/**
	 * remove first value from the down queue
	 */
	public void removeDown() {
		downQueue.remove(0);
	}

	/**
	 * @return string id of elevator
	 */
	public String getID() {
		return id;
	}

	/**
	 * @param string id of elevator
	 */
	public void setID(String id) {
		this.id = id;
	}

	/**
	 * @return ArrayList<Integer> the down queue
	 */
	public ArrayList<Integer> getDownQueue() {
		return downQueue;
	}

	/**
	 * @return ArrayList<Integer> the up queue
	 */
	public ArrayList<Integer> getUpQueue() {
		return upQueue;
	}

	/**
	 * @return int the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param int port of elevator
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * @param int floorNum
	 */
	public void addToUp(int floorNum) {
		upQueue.add(floorNum);
	}

	/**
	 * @param int floorNum
	 */
	public void addToDown(int floorNum) {
		downQueue.add(floorNum);
	}

	/**
	 * @return InetAddress address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * @param InetAddress address
	 */
	public void setAddress(InetAddress address) {
		this.address = address;
	}

	/**
	 * @return String timestamp
	 */
	public String getTimestamp() {
		return this.timestamp;
	}

	/**
	 * @return String status
	 */
	public String getStatus() {
		return this.status;
	}

	/**
	 * @param string status
	 */
	public void setStatus(String status) {
		this.status = status;
		setDestination();
	}
	
	/**
	 * @return int destination
	 */
	public int getDestination() {
		return this.currDestination;
	}

	/**
	 * 
	 * @param int destination
	 */
	public void addDestination(int destination) {
		this.destinations.add(destination);
		if(destinations.size() == 1) {
			currDestination = destination;
		}
	}
	
	/**
	 * Set the destination
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
	
	/**
	 * @param string timestamp
	 */
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

	/**
	 * @param boolean add
	 * @param int floorNum
	 */
	public void setElevatorLamps(boolean add, int floorNum) {
		if (add) {
			this.elevatorLampArray[floorNum - 1] = true;
		} else {
			this.elevatorLampArray[floorNum - 1] = false;
		}

	}
}
