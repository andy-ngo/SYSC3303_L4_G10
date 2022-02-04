import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 
 */

/**
 * @author Ali Fahd
 *
 */
class SchedulerTest extends Scheduler {
	Scheduler scheduler = new Scheduler();
	FloorSubsytem floorSystem = new FloorSubsytem(scheduler);
	
	@Test
	void testRequests() {
		floorSystem.addFloorRequest("elevator.txt");
		scheduler.putRequests(floorSystem.getRequests());
		Assertions.assertFalse(scheduler.getEmptyRequests());
		scheduler.getRequests();
		Assertions.assertTrue(scheduler.getEmptyRequests());
	}
	
	@Test
	void testArrivalSensor() {
		scheduler.putArrivalSensor(2,true);
		Assertions.assertFalse(scheduler.getEmptyArrivalSensor());
		scheduler.getArrivalSensor();
		Assertions.assertTrue(scheduler.getEmptyArrivalSensor());
	}
}
