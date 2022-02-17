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
		System.out.println("Requests issued to scheduler.");
		notifyAll();
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
		System.out.println("Requests handed off to elevator.");
		emptyRequests = true;	// requests have been taken from scheduler
		notifyAll();
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
}
