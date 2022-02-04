import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import junit.framework.Assert;

/**
 * 
 */

/**
 * @author Ali Fahd
 *
 */
class FloorSubsytemTest {
	Scheduler scheduler = new Scheduler();
	FloorSubsytem floorSystem = new FloorSubsytem(scheduler);
	
	@Test
	void testImportRequests() {
		floorSystem.addFloorRequest("elevator.txt");
		Assertions.assertEquals(4, floorSystem.getRequests().size());
		Assertions.assertEquals("Elevator Request: Time - 14:05:15.0 - Floor Origin - 2 - Direction - Up - Floor Destination - 4", "Elevator Request: Time - 14:05:15.0 - Floor Origin - 2 - Direction - Up - Floor Destination - 4");
	}
	
	@Test
	void testRequestsDelete() {
		floorSystem.deleteRequest(1);
		Assertions.assertEquals(3, floorSystem.getRequests().size());
		floorSystem.clearRequests();
		Assertions.assertEquals(0, floorSystem.getRequests().size());
	}
	
	@Test
	void testArrivalSensors() {
		floorSystem.setArrivalSensor(2, true);
		Assertions.assertTrue(floorSystem.getArrivalSensors().get(2));
		floorSystem.setArrivalSensor(2, false);
		Assertions.assertFalse(floorSystem.getArrivalSensors().get(2));
	}
}
