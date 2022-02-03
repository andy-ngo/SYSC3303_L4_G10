import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ali Fahd
 *
 */
public class Scheduler {
	private ArrayList<FloorRequest> requests = new ArrayList<FloorRequest>();
	private int ASNumber = 0;
	private boolean emptyRequests = true; 
	private boolean emptyArrivalSensor = true; 


	public synchronized void putRequests(ArrayList<FloorRequest> requests) {
		while (!emptyRequests) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		this.requests = requests;
		emptyRequests = false;
		System.out.println("Requests issued to scheduler.");
		notifyAll();
	}

	public synchronized ArrayList<FloorRequest> getRequests() {
		while (emptyRequests) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		System.out.println("Requests handed off to elevator.");
		emptyRequests = true;
		notifyAll();
		return requests;
	}
	
	public synchronized void putArrivalSensor(int ASNumber) {
		while (!emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		this.ASNumber = ASNumber;
		emptyArrivalSensor = false;
		notifyAll();
	}

	public synchronized int getArrivalSensor() {
		while (emptyArrivalSensor) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		emptyArrivalSensor = true;
		notifyAll();
		return ASNumber;
	}
}
