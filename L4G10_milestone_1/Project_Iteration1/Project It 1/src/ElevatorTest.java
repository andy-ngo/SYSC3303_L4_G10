/*
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 1.0V
 * 
 * Description:
 * This is just a test class to make sure the door status, motor direction and lamp status are working
 */

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ElevatorTest{

	private Elevator elevator;
	private int id = 1;
	private int curr_floor = 0;
	
	/*
	 * Will test to see if the elevator door will open and close
	 */
	@Test
	public void testDoorStatus()
	{
		elevator = new Elevator(id,curr_floor);
		elevator.closeDoor();
		assertEquals(false,elevator.doorStatus());
		elevator.openDoor();
		assertEquals(true,elevator.doorStatus());
	}
	
	/*
	 * Will test to see if the motor direction is correct
	 */
	@Test
	public void testElevatorDirection()
	{
		elevator = new Elevator(id,curr_floor);
		elevator.go_Up();
		assertEquals(Elevator_Motor.Up,elevator.getDirection());
		elevator.go_Down();
		assertEquals(Elevator_Motor.Down,elevator.getDirection());
		elevator.stop();
		assertEquals(Elevator_Motor.Stop,elevator.getDirection());
	}
	
	/*
	 * Will test to see if the lamp status is up to date
	 */
	@Test
	public void testLampStatus()
	{
		elevator = new Elevator(id,1);
		elevator.lampOff();
		assertEquals(false,elevator.lampStatus());
		elevator.lampOn();
		assertEquals(true,elevator.lampStatus());
	}

}
