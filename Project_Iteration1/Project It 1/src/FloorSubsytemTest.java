import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/**
 * Test class for FloorSubsytem class. Uses local elevator.txt file created with sample data for tests.
 * 
 * @author Ali Fahd
 *
 */
class FloorSubsytemTest {
	Scheduler scheduler = new Scheduler();
	FloorSubsytem floorSystem = new FloorSubsytem(scheduler);
	
	/**
	 * Test method for testing importing requests into FloorSubsystem.
	 * Checks if all of them are imported and if data is as expected based on sample data.
	 *
	 */
	@Test
	void testImportRequests() {
		floorSystem.addFloorRequest("elevator.txt");	// import data from local file
		Assertions.assertEquals(4, floorSystem.getRequests().size());
		Assertions.assertEquals("Elevator Request: Time - 14:05:15.0 - Floor Origin - 2 - Direction - Up - Floor Destination - 4", "Elevator Request: Time - 14:05:15.0 - Floor Origin - 2 - Direction - Up - Floor Destination - 4");
	}
	
	/**
	 * Test method for testing deleting requests that were previously imported into FloorSubsystem.
	 * Checks if one is removed and then checks if all were removed.
	 *
	 */	
	@Test
	void testRequestsDelete() {
		floorSystem.deleteRequest(1);	// remove request at index 1
		Assertions.assertEquals(3, floorSystem.getRequests().size());
		floorSystem.clearRequests();	// remove all requests in FloorSubsytem
		Assertions.assertEquals(0, floorSystem.getRequests().size());
	}
	
	/**
	 * Test method for testing if arrival sensors are set properly.
	 *
	 */	
	@Test
	void testArrivalSensors() {
		floorSystem.setArrivalSensor(2, true);	// sets floor 2 arrival sensor to on
		Assertions.assertTrue(floorSystem.getArrivalSensors().get(2));
		floorSystem.setArrivalSensor(2, false);	//sets floor 2 arrival sensor to off
		Assertions.assertFalse(floorSystem.getArrivalSensors().get(2));
	}
}
