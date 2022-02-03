/**
 * @author schararaislam
 *
 */
public class Scheduler implements Runnable {
	private boolean empty = false; //getting message
	private Object messagelock = new Object();
	private Thread messagehandler;
	private Thread FloorSubsystemThread;
	private Thread ElevatorSystemThread;
	
	
	public Scheduler() {
		messagehandler = new Thread(messagehandler, "scheduler message");
		messagehandler.start();
		
		FloorSubsystemThread = new Thread(this.FloorSubsystemThread);
		FloorSubsystemThread.start();
		
		ElevatorSystemThread = new Thread(this.ElevatorSystemThread);
		ElevatorSystemThread.start();
	}
	
	public static void main(String[] args) {
		Scheduler s1 = new Scheduler();
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
/*
	public synchronized void FloorSubsystem() {
		while (empty) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				return;
			}
		
		notifyAll();
		}
	}
	*/
	/*
	public synchronized void ElevatorSystem() {
		while (empty) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				return;
			}
		
		notifyAll();
	}
	*/
	
	

}