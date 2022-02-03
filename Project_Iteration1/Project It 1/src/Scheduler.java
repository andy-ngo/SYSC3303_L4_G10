/**
 * @author schararaislam
 *
 */
public class Scheduler implements Runnable {
	private String[] communication = new String[] { null, null };
	private boolean empty = true; 


	// using the synchronized with Floorsubsystem to communicate with the elevator thread
	public synchronized void FloorSystem(String[] floorcom) {
		while (!empty) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		communication[0] = floorcom[0];
		communication[1] = floorcom[1];
		empty = false;
		System.out.println("Floor System " + Thread.currentThread().getName());
		notifyAll();

	}

	public synchronized String[] ElevatorSystem(String[] elevatorcom) {
		while (!empty) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				System.out.println(e);
			}
		}
		communication[0] = elevatorcom[0];
		communication[1] = elevatorcom[1];
		empty = false;
		System.out.println("Elevator System " + Thread.currentThread().getName());
		notifyAll();
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
