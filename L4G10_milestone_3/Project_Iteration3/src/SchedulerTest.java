import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test class for Scheduler class. Uses local elevator.txt file created with sample data for tests.
 * 
 * @author Ali Fahd
 *
 */
class SchedulerTest extends Scheduler {
	Scheduler scheduler = new Scheduler();
	FloorSubsytem floorSubystem = new FloorSubsytem(scheduler);
	
	/**
	 * Test method for testing requests in scheduler. Checks if requests are put in the scheduler and then removed from scheduler.
	 *
	 */
	@Test
	void testRequests() {
		floorSubystem.addFloorRequest("elevator.txt");	//imports sample data
		for(FloorRequest fr: floorSubystem.getRequests()) {
			scheduler.putRequest(fr);
		}
		Assertions.assertFalse(scheduler.getEmptyRequests());	//checks if data is placed in scheduler
		for(FloorRequest fr: floorSubystem.getRequests()) {
			scheduler.getRequest();
		}		
		Assertions.assertTrue(scheduler.getEmptyRequests());	//checks if data has left scheduler
	}
	
	/**
	 * Test method for testing arrival sensors in scheduler. Checks if sensor notifications are put in the scheduler and then removed from scheduler.
	 *
	 */
	@Test
	void testArrivalSensor() {
		scheduler.putArrivalSensor(2,true);
		Assertions.assertFalse(scheduler.getEmptyArrivalSensor());	//checks if sensor data is placed in scheduler
		scheduler.getArrivalSensor();
		Assertions.assertTrue(scheduler.getEmptyArrivalSensor());	//checks if sensor data has been removed from scheduler
	}
}
