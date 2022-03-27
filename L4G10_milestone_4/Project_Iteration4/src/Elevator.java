/**
 * @author Andy Ngo
 * Version: 1.0V
 * 
 * The data structure of the elevator and has all the elevator propterties that can be used
 * in tests cases and the elevator subsystem
 */

import java.net.InetAddress;
import java.util.*;

public class Elevator {
	//Initialize variables
	String id = "";
	int port;
	private InetAddress address;
	private ArrayList<Integer> upQueue;
	private ArrayList<Integer> downQueue;
	private int currentFloor, currDestination;
	private ElevatorMotor elevatorMotor;
	private ReadPropertyFile r;
	private boolean elevatorLampArray[];
	private int error;
	private String timestamp;
	private String status;

	private ArrayList<Integer> destinations;
	
	/**
	 * Initialize variables
	 *
	 * @param id, the elevator id
	 * @param port, the elevator port
	 * @param address, the elevator address 
	 * @param currentFloor, the current floor that the elevator is on
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
		elevatorMotor = ElevatorMotor.STOP;
		r = new ReadPropertyFile();
		this.error = 0;
		this.elevatorLampArray = new boolean[r.getNumFloors()];
		
		for (int i = 0; i < r.getNumFloors(); i++) {
			elevatorLampArray[i] = false;
		}
		this.timestamp = " ";
		this.status = "waiting";
	}

	/**
	 * Elevator constructor
	 */
	public Elevator() 
	{
	}

	// Getters and setters for the elevator
	
	/**
	 * Getting the current floor that the elevator is on
	 * 
	 * @return currentFloor
	 */
	public int getCurrentFloor() 
	{
		return currentFloor;
	}
	
	/**
	 * Will set the current floor that the elevator is on
	 * 
	 * @param currentFloor, the current floor number
	 */
	public void setCurrentFloor(int currentFloor) 
	{
		this.currentFloor = currentFloor;
	}

	/**
	 * Will get the error 
	 *
	 * @return error
	 */
	public int getError() 
	{
		return error;
	}

	/**
	 * Will set the error
	 * 
	 * @param x, the error number
	 */
	public void setError(int x) 
	{
		this.error = x;
	}

	/**
	 * Will get the elevator motor direction
	 * 
	 * @return elevatorMotor
	 */
	public ElevatorMotor getElevatorMotor() 
	{
		return elevatorMotor;
	}

	/**
	 * This will set the elevator motor to the desired direction
	 * 
	 * @param elevatorMotor, the desired direction for the elevator motor
	 */
	public void setElevatorMotor(ElevatorMotor elevatorMotor) 
	{
		this.elevatorMotor = elevatorMotor;
	}

	/**
	 * Will remove the first value from the up queue
	 */
	public void removeUp() 
	{
		upQueue.remove(0);
	}

	/**
	 * Will remove the first value from the down queue
	 */
	public void removeDown() 
	{
		downQueue.remove(0);
	}

	/**
	 * Will get the ID of the elevator
	 * 
	 * @return id of elevator
	 */
	public String getID() 
	{
		return id;
	}

	/**
	 * Will set the ID of the elevator
	 * 
	 * @param id, the id for the elevator
	 */
	public void setID(String id) 
	{
		this.id = id;
	}

	/**
	 * This will return the down queue of the elevator
	 * 
	 * @return the down queue
	 */
	public ArrayList<Integer> getDownQueue() 
	{
		return downQueue;
	}

	/**
	 * This will return the up queue of the elevator
	 * 
	 * @return the up queue
	 */
	public ArrayList<Integer> getUpQueue() 
	{
		return upQueue;
	}
	
	/**
	 * Will add to the elevator up queue
	 * 
	 * @param floorNum, desired floor to add to the queue
	 */
	public void addToUp(int floorNum) 
	{
		upQueue.add(floorNum);
	}

	/**
	 * Will add to the elevator down queue
	 * 
	 * @param floorNum, desired floor to add to the queue
	 */
	public void addToDown(int floorNum) 
	{
		downQueue.add(floorNum);
	}

	/**
	 * Will get the port of the elevator
	 * 
	 * @return the port
	 */
	public int getPort() 
	{
		return port;
	}

	/**
	 * Will set the port of the elevator
	 * 
	 * @param port, the desire port for the elevator
	 */
	public void setPort(int port) 
	{
		this.port = port;
	}

	/**
	 * Will get the address of the elevator
	 * 
	 * @return address
	 */
	public InetAddress getAddress() 
	{
		return address;
	}

	/**
	 * Will set the address for the elevator
	 * 
	 * @param address, desire address for the elevator
	 */
	public void setAddress(InetAddress address) 
	{
		this.address = address;
	}

	/**
	 * This will return the time stamp of the elevator
	 * 
	 * @return timestamp
	 */
	public String getTimestamp() 
	{
		return this.timestamp;
	}
	
	/**
	 * This method will be used to set a time stamp
	 * 
	 * @param timestamp, the time stamp
	 */
	public void setTimestamp(String timestamp) 
	{
		this.timestamp = timestamp;
	}

	/**
	 * This will return the status of the elevator
	 * 
	 * @return status
	 */
	public String getStatus() 
	{
		return this.status;
	}

	/**
	 * This will set the status of the elevator
	 * 
	 * @param status, desired status for the elevator
	 */
	public void setStatus(String status) 
	{
		this.status = status;
		setDestination();
	}
	
	/**
	 * This will get the destination of the elevator
	 * 
	 * @return destination
	 */
	public int getDestination() 
	{
		return this.currDestination;
	}

	/**
	 * This will be used to add a destination for the elevator
	 * 
	 * @param destination, the destination that will be added to the elevator requests
	 */
	public void addDestination(int destination) 
	{
		this.destinations.add(destination);
		if(destinations.size() == 1) 
		{
			currDestination = destination;
		}
	}
	
	/**
	 * This will set the destination for the elevator
	 */
	private void setDestination() 
	{
		if(this.status.contains("arr"))
			{
				destinations.remove(0);
				if(destinations.size() != 0) 
				{
					currDestination = destinations.get(0);
				}
				else 
				{
					currDestination = -1; 
				}
			}
	}
	
	/**
	 * This will sort the arrays of the elevator
	 */
	public void sortArrays() 
	{
		Collections.sort(this.upQueue);
		Collections.sort(this.downQueue);
		Collections.reverse(this.downQueue);
	}

	/**
	 * This will be used to set the elevator lamps
	 * 
	 * @param add, if it is true it will add the floor number to the lamp array
	 * @param floornum, the desired floor
	 */
	public void setElevatorLamps(boolean add, int floornum) {
		if (add) 
		{
			this.elevatorLampArray[floornum - 1] = true;
		} 
		else 
		{
			this.elevatorLampArray[floornum - 1] = false;
		}

	}
}
