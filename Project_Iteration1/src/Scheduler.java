/**
 * @author schararaislam
 *
 */
public class Scheduler implements Runnable {



	private boolean empty = false; 
	

	Thread FloorSubsystemThread = new Thread(this.FloorSubsystemThread);
	FloorSubsystemThread.start();
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
	Thread ElevatorSystemThread = new Thread(this.ElevatorSystemThread);
	ElevatorSystem.start();
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
