import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Scharara Islam
 * @author Ali Fahd
 * A scheduler class that allows to communicate between the Elevator class and FloorSubsystem using Thread.
 * This class is responsible for accepting request from the Floor and routing the elevator to their requested floor
 */
public class Scheduler {
	private ArrayList<FloorRequest> requests = new ArrayList<FloorRequest>();	// list of request passed by floor subsystem
	private String[] arrivalSensor = {"", ""};	//temp field for passing arrival sensor information
	private boolean emptyRequests = true; // check for getting requests
	private boolean emptyArrivalSensor = true;	// check for getting arrival sensors
	private HashMap<Integer, Integer> elevatorArrivals = new HashMap<Integer, Integer>();	// keeps track of elevator arrivals key=elevator number, value = arrival floor number
	
	/**
     * Enum for the states
     */
    public enum SchedulerStates {
    	EmptyRequests,
    	ReceivedRequests,
    	EmptyArrivalSensors,
    	ReceivedArrivalSensors
    }
    
    /**
     * Constructor for Scheduler class, sets states to empty.
     */
    public Scheduler() {
    	stateMachine(SchedulerStates.EmptyRequests);
    	stateMachine(SchedulerStates.EmptyArrivalSensors);
    }
    
    /**
	 * This will be the state machine controlling the scheduler status by following the state given
	 * @param SchedulerStates state - will be used to change the state
	 */
	public void stateMachine(SchedulerStates state)
	{
		switch(state)
		{
			case EmptyRequests:
				System.out.println("SCHEDULER: No Requests\n");
				break;
			
			case ReceivedRequests:
				System.out.println("SCHEDULER: Contains Requests\n");
				break;
				
			case EmptyArrivalSensors:
				System.out.println("SCHEDULER: No Arrival Sensor Information\n");
				break;
				
			case ReceivedArrivalSensors:
				System.out.println("SCHEDULER: Contains Arrival Sensor Information\n");
				break;
				
		}
		
	}
	
	/**
     * Method puts request into the scheduler. Updates empty request status.
     * @param ArrayList<FloorRequest> requests passed to put in scheduler
     */
	public synchronized void putRequests(ArrayList<FloorRequest> requests) {		
		while (!emptyRequests) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		this.requests = requests;
		emptyRequests = false; 	// request have been put in scheduler
		System.out.println("SCHEDULER: Requests issued to scheduler.");
		notifyAll();
    	stateMachine(SchedulerStates.ReceivedRequests);
	}
	
	/**
     * Method gets request that were put into the scheduler. Updates empty request status.
     * @return ArrayList<FloorRequest> requests that were passed to the scheduler
     */
	public synchronized ArrayList<FloorRequest> getRequests() {		
		while (emptyRequests) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		System.out.println("SCHEDULER: Requests handed off to elevator.");
		emptyRequests = true;	// requests have been taken from scheduler
		notifyAll();
    	stateMachine(SchedulerStates.EmptyRequests);
		return requests;
	}
	
	/**
     * Method puts arrival sensor into the scheduler. Updates empty arrival sensor status.
     * @param int ASNumber number of floor
     * @param Boolean status of sensor
     */
	public synchronized void putArrivalSensor(int ASNumber, Boolean status) {
		while (!emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		arrivalSensor[0] = Integer.toString(ASNumber);	//stores arrival sensor data in field
		arrivalSensor[1] = Boolean.toString(status);
		emptyArrivalSensor = false; 	// arrival sensor has been put in scheduler
		notifyAll();
    	stateMachine(SchedulerStates.ReceivedArrivalSensors);
	}

	/**
     * Method gets arrival sensors that were put into the scheduler. Updates empty arrival sensor status.
     * @return String[] arrival sensor that were passed to the scheduler
     */
	public synchronized String[] getArrivalSensor() {
		while (emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		emptyArrivalSensor = true;	// arrival sensor data has been taken from scheduler
		notifyAll();
		return arrivalSensor;
	}
	
	/**
     * Method gets empty request status.
     * @return boolean empty request status.
     */	
	public boolean getEmptyRequests() {
		return emptyRequests;
	}
	
	/**
     * Method gets empty arrival sensor status.
     * @return boolean empty arrival sensor status.
     */	
	public boolean getEmptyArrivalSensor() {
		return emptyArrivalSensor;
	}
	
	/**
     * Method sets elevator arrival values.
     * @param int elevatorNumber - the elevator that has arrived on the respective floor.
     * @param int floorNuumber - the floor the elevator has arrived on
     */	
	public void setElevatorArrival(int elevatorNumber, int floorNumber) {
		elevatorArrivals.remove(elevatorNumber);
		elevatorArrivals.put(elevatorNumber, floorNumber);
		elevatorArrivals.get(elevatorNumber);
		System.out.println("SCHEDULER: {Elevator #: "+ elevatorNumber +" - Floor #: "+ elevatorArrivals.get(elevatorNumber) +"} - " + elevatorArrivals);
	}
	
	/**
     * Method sets elevator arrival values.
     * @param int num - the elevator number
     * @return Integer - arrival floor number of elevator
     */	
	public Integer getFloorStatus(int num) {
		return elevatorArrivals.get(num);
	}
}
