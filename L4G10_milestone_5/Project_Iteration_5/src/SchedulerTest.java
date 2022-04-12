import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import junit.framework.TestCase;

/*
 * Test for class scheduler. Tests check to see if requests are distributed to the appropriate elevator queue.
 */
public class SchedulerTest extends TestCase {
	private Scheduler scheduler;
	private FloorSubsystem floor;
	private FloorRequest floorRequest;
	private byte[] data;
	private DatagramSocket socket;

	protected void setUp() throws Exception {
		super.setUp();
		scheduler = new Scheduler();
		floor = new FloorSubsystem("testRequests.txt");

		floorRequest = floor.getRequests().remove(0);
		data = floorRequest.toString().getBytes();

		socket = new DatagramSocket(50);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		scheduler.closeSockets();
		socket.close();
	}
	
	/**
	 * Method checks if request is added to appropriate queue
	 */
	public void testPriority() {
		ArrayList<Elevator> elevators = new ArrayList<>();
		DatagramPacket p = new DatagramPacket(new byte[100], 100);
		Elevator e = new Elevator("Elevator1", p.getPort(), p.getAddress(), 0);
		elevators.add(e);
		
		scheduler.setElevators(elevators);
		assertEquals(e.getDownQueue().size(), 0);
		assertEquals(e.getUpQueue().size(), 0);
		
		scheduler.checkPriority(2, "UP", 4);
		
		//add 2 requests to go up to floor 2, then 4
		assertEquals(e.getUpQueue().size(), 2); 
		assertEquals(e.getDownQueue().size(), 0);
		
	}
	
	/**
	 * method updates the direction of the elevator
	 */
	public void testUpdate() {
		ArrayList<Elevator> elevators = new ArrayList<>();
		DatagramPacket p = new DatagramPacket(new byte[100], 100);
		Elevator e = new Elevator("Elevator1", p.getPort(), p.getAddress(), 0);
		elevators.add(e);
		
		scheduler.setElevators(elevators);
		assertEquals(e.getDirection().toString(), "STOP");
		assertEquals(e.getDownQueue().size(), 0);
		assertEquals(e.getUpQueue().size(), 0);
		
		scheduler.checkPriority(2, "UP", 4);
		scheduler.checkSend(e);
		assertEquals(e.getDirection().toString(), "UP");
		assertEquals(e.getUpQueue().size(), 1);
		assertEquals(e.getDownQueue().size(), 0);
		
	}
}