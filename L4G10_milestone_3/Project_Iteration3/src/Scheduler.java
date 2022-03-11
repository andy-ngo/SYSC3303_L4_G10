import java.util.*;
import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

/**
 * @author Scharara Islam
 * @author Ali Fahd
 * A scheduler class that allows to communicate between the Elevator class and FloorSubsystem using Thread.
 * This class is responsible for accepting request from the Floor and routing the elevator to their requested floor
 */
public class Scheduler {
	private Queue<FloorRequest> requests = new LinkedList<>();	// list of request passed by floor subsystem
	private boolean emptyRequest = true; // check for getting requests
	private boolean emptyArrivalSensor = true;	// check for getting arrival sensors
	private HashMap<Integer, Integer> elevatorArrivals = new HashMap<Integer, Integer>();	// keeps track of elevator arrivals key=elevator number, value = arrival floor number
	private DatagramPacket sendPacket,receivePacket;
	private DatagramSocket elevatorSendReceiveSocket, floorSendReceiveSocket;

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

    	
		try
    	{
    		elevatorSendReceiveSocket = new DatagramSocket(98);
    		floorSendReceiveSocket = new DatagramSocket(22);
    	} catch(SocketException se)
    	{
    		se.printStackTrace();
    		System.exit(1);
    	}
    	
    }

	private void sendReceive()
    {
    	byte[] dataByte = new byte[100];
    	receivePacket = new DatagramPacket(dataByte, dataByte.length);
    	while(true)
    	{
    	
    	}
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
				System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: No Requests\n");
				break;
			
			case ReceivedRequests:
				System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: Contains Requests\n");
				break;
				
			case EmptyArrivalSensors:
				System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: No Arrival Sensor Information\n");
				break;
				
			case ReceivedArrivalSensors:
				System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: Contains Arrival Sensor Information\n");
				break;
				
		}
		
	}
	
	/**
     * Method puts request into the scheduler. Updates empty request status.
     * @param ArrayList<FloorRequest> requests passed to put in scheduler
     */
	public synchronized void putRequest(FloorRequest request) {	
		try 
		{
			floorSendReceiveSocket.receive(receivePacket);
		} catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		this.requests.add(request);
		System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: Requests in queue: " + requests.size());
		emptyRequest = false; 	// request have been put in scheduler
		notifyAll();
    	stateMachine(SchedulerStates.ReceivedRequests);
	}
	
	/**
     * Method gets request that were put into the scheduler. Updates empty request status.
     * @return ArrayList<FloorRequest> requests that were passed to the scheduler
     */
	public synchronized FloorRequest getRequest() {		
		while (emptyRequest) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		FloorRequest fr = requests.remove();
		System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: Request handed off to elevator.\n");
		if (requests.size() == 0) {
			emptyRequest = true;	// all requests will be taken from scheduler after the remove
	    	stateMachine(SchedulerStates.EmptyRequests);
		}
		notifyAll();
		return fr;
	}
	
	/**
     * Method puts arrival sensor into the scheduler. Updates empty arrival sensor status.
     * @param int ASNumber number of floor
     * @param Boolean status of sensor
     */
	public synchronized void putArrivalSensor(int elevatorNumber, int floorNumber) {
		while (!emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		
		elevatorArrivals.remove(elevatorNumber);
		elevatorArrivals.put(elevatorNumber, floorNumber);
		elevatorArrivals.get(elevatorNumber);
		
    	stateMachine(SchedulerStates.ReceivedArrivalSensors);
		System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: {Elevator #: "+ elevatorNumber +" - Floor #: "+ elevatorArrivals.get(elevatorNumber) +"} - " + elevatorArrivals);
		
		emptyArrivalSensor = false; 	// arrival sensor has been put in scheduler
		
		notifyAll();
	}

	/**
     * Method gets arrival sensors that were put into the scheduler. Updates empty arrival sensor status.
     * @return String[] arrival sensor that were passed to the scheduler
     */
	public synchronized HashMap<Integer, Integer> getArrivalSensor() {
		while (emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		
		emptyArrivalSensor = true;	// arrival sensor data has been taken from scheduler
		
		System.out.println(Timestamp.from(Instant.now()) + "  -  SCHEDULER: Notifying Floor Subsystem that request has been serviced.");
    	stateMachine(SchedulerStates.EmptyArrivalSensors);
    	
		notifyAll();
		
		return elevatorArrivals;
	}
	
	/**
     * Method gets empty request status.
     * @return boolean empty request status.
     */	
	public boolean getEmptyRequests() {
		return emptyRequest;
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
     * @param int num - the elevator number
     * @return Integer - arrival floor number of elevator
     */	
	public Integer getFloorStatus(int num) {
		return elevatorArrivals.get(num);
	}
	

	/**
     * Method gets requests queue in scheduler.
     * @return Queue<FloorRequest> -requests in scheduler
     */	
	public Queue<FloorRequest> getRequests() {
		return requests;
	}
}
