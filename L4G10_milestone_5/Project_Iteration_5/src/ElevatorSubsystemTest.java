/**
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 5.0V
 * 
 * Description:
 * This is just a test class to make sure the motor direction and the current floor of the Elevator and ElevatorSubsystem are working
 */

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.net.DatagramPacket;
import java.util.ArrayList;

public class ElevatorSubsystemTest {
	private ElevatorSubsystem elevator;
	private Elevator e;
	
	/**
	 * This test function will make sure that the ElevatorSubsystem motor state works
	 */
	@Test
	void testMotorState() 
	{
		ArrayList<Elevator> elevators = new ArrayList<>();
		DatagramPacket p = new DatagramPacket(new byte[100], 100);
		elevator = new ElevatorSubsystem("Elevator1");
		e = new Elevator("Elevator1", p.getPort(), p.getAddress(), 0);
		elevators.add(e);
		
		e.setElevatorMotor(ElevatorMotor.STOP);
		assertEquals(elevator.getMotorState().toString(),"STOP");
		assertEquals(e.getElevatorMotor().toString(),"STOP");
		
		e.setElevatorMotor(ElevatorMotor.DOWN);
		assertEquals(e.getElevatorMotor().toString(),"DOWN");
		
		e.setElevatorMotor(ElevatorMotor.UP);
		assertEquals(e.getElevatorMotor().toString(),"UP");
	}
	
	/**
	 * This test function will make sure that the ElevatorSubsystem current floor is correct
	 */
	@Test
	void testCurrentFloor() 
	{
		ArrayList<Elevator> elevators = new ArrayList<>();
		DatagramPacket p = new DatagramPacket(new byte[100], 100);
		elevator = new ElevatorSubsystem("Elevator1");
		e = new Elevator("Elevator1", p.getPort(), p.getAddress(), 0);
		elevators.add(e);
		
		e.setCurrentFloor(1);
		assertEquals(elevator.getCurrentFloor(),1);
		assertEquals(e.getCurrentFloor(),1);
		
		e.setCurrentFloor(4);
		assertEquals(e.getCurrentFloor(),4);
		
		e.setCurrentFloor(22);
		assertEquals(e.getCurrentFloor(),22);
	}
	
	/**
	 * This test function will make sure that the ElevatorSubsystem ID is correct
	 */
	@Test
	void testID() 
	{
		ArrayList<Elevator> elevators = new ArrayList<>();
		DatagramPacket p = new DatagramPacket(new byte[100], 100);
		elevator = new ElevatorSubsystem("Elevator1");
		e = new Elevator("Elevator1", p.getPort(), p.getAddress(), 0);
		elevators.add(e);
		
		assertEquals(elevator.getID(),"Elevator1");
		assertEquals(e.getID(),"Elevator1");
		
		e.setID("Elevator2");
		elevator = new ElevatorSubsystem("Elevator2");
		assertEquals(elevator.getID(),"Elevator2");
		assertEquals(e.getID(),"Elevator2");
		
		e.setID("Elevator4");
		elevator = new ElevatorSubsystem("Elevator4");
		assertEquals(elevator.getID(),"Elevator4");
		assertEquals(e.getID(),"Elevator4");
	}

}

