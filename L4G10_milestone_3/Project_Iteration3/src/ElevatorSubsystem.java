/*
 * Author: Andy Ngo, Karim Mahrous
 * Student ID: 101132278
 * Version: 3.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * A state machine is implemented to organize some parts of the code.
 * This class will be synchronizing with the scheduler class.
 */

import java.util.ArrayList;
import java.net.*;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;

public class ElevatorSubsystem implements Runnable 
{
	//initialize variables
	private Scheduler s;
	private Elevator_Motor motor;
	private int id;
	private boolean buttons[];
	private boolean open_Door = false;
	private boolean lamp_Status = false;
	private int curr_Floor;
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendReceiveSocket;
	
	
	//initialize constructors
	public ElevatorSubsystem(Scheduler s, int id, int curr_Floor)
	{
		this.s = s;
		this.id = id;
		this.open_Door = false;
		this.lamp_Status = false;
		this.curr_Floor = curr_Floor;
		
		try 
		{
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException se) 
		{ 
			se.printStackTrace();
			System.exit(1);
		}
	}

	public void initailize()
	{
		byte[] dataByte = new byte[100];
		
		try 
		{
			this.sendPacket = new DatagramPacket(dataByte,dataByte.length, InetAddress.getLocalHost(),99);
		} catch (IOException e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
		
		try 
		{
			this.sendReceiveSocket = new DatagramSocket();
		} catch (IOException e) 
		{ 
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * This will be the state machine controlling the elevator movement by following the state given
	 * @param ElevatorStates - will be used to change the state of the elevator
	 */
	public void stateMachine(ElevatorStates state)
	{
		switch(state)
		{
			//idle
			case IDLE_STATE:
				byte[] data = new byte[100];
				receivePacket = new DatagramPacket(data, data.length);
				
				try 
				{
					this.sendReceiveSocket = new DatagramSocket();
				} catch (IOException e) 
				{ 
					e.printStackTrace();
					System.exit(1);
				}
				
				String receivePacketData = new String(receivePacket.getData(), 0, this.receivePacket.getLength());
				System.out.println("Received: " + new String(receivePacket.getData(),0,this.receivePacket.getLength()));
				
				if(receivePacketData.equals("Waiting"))
				{
					String elevatorHasRequest = receivePacketData;
					byte[] sendData = elevatorHasRequest.getBytes();
					try
					{
						this.sendPacket = new DatagramPacket(sendData,sendData.length,InetAddress.getLocalHost(),99);
					} catch(UnknownHostException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
					try
					{
						this.sendReceiveSocket.send(this.sendPacket);
					} catch(IOException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
				}
				else
				{
				}
				System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR SUBSYSTEM: Waiting for requests...\n");
				break;
			
			case OPERATE_STATE:
				System.out.println(Timestamp.from(Instant.now()) + "  -  ~~Operate State~~");
				break;
			//go up
			case UP_STATE:
				go_Up();
				break;
				
			//go down
			case DOWN_STATE:
				go_Down();
				break;

			//stop/unloading
			case STOP_STATE:
				stop();
				System.out.println(Timestamp.from(Instant.now()) + "  -  Elevator notifying Scheduler of arrival...");
				//s.putArrivalSensor(id, curr_Floor);

				byte[] dataByte = new byte[100];
				try 
				{
					this.sendPacket = new DatagramPacket(dataByte,dataByte.length, InetAddress.getLocalHost(),99);
				} catch (IOException e) 
				{ 
					e.printStackTrace();
					System.exit(1);
				}
				
				try 
				{
					this.sendReceiveSocket.send(this.sendPacket);
				} catch (IOException e) 
				{ 
					e.printStackTrace();
					System.exit(1);
				}

				break;
				
		}
		
	}

	/*
	 * This function will check whether the floor that the elevator is trying to get to is higher or lower and will keep going until it reaches the destination floor
	 * 
	 * @param ArrayList<FloorRequest> request - will be used in the function to check whether there are any requested floors on the list.
	 */
	public boolean operate(FloorRequest request)
	{
		
		stateMachine(ElevatorStates.OPERATE_STATE);
		
		//initialize variables to use in the loop
		lampOn();
		closeDoor();

		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR -  Going to next request");

		//will check if there are any more floors on the request list and will either go up or down depending on current floor
		if(curr_Floor < request.getFloorOrigin())
		{
			go_Up();
			System.out.println("Waiting for next reqest....");
			System.out.println("Packet recieved from floorsubsystem....");
			System.out.println("Parsing packet.....");
			System.out.println("Parsing complete processing request....");
			System.out.println("ELEVATOR: To the next requested floor: " + request.getFloorOrigin());
			System.out.println("Travelling to the requested floor....");

		}
		else if(curr_Floor > request.getFloorOrigin())
		{
			go_Down();
			System.out.println("Waiting for next reqest....");
			System.out.println("Packet recieved from floorsubsystem....");
			System.out.println("Parsing packet.....");
			System.out.println("Parsing complete processing request....");
			System.out.println("ELEVATOR: To the next requested floor: " + request.getFloorOrigin());
			System.out.println("Travelling to the requested floor....");
			
		}
		
		curr_Floor = request.getFloorOrigin();
		
		//print out the current floor and destination floor
		System.out.println(Timestamp.from(Instant.now()) + "  -  ###########################");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ##   Current Floor: " + curr_Floor + "   ##");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ## Destination Floor: " + request.getFloorDestination()+ " ##");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ###########################");
		
		//keep checking if current floor is the same as the destination floor or else keep looping
		while(curr_Floor != request.getFloorDestination() )
		{
			System.out.println(Timestamp.from(Instant.now()) + "  -  ======= ELEVATOR " + id + " =======");
			System.out.println(Timestamp.from(Instant.now()) + "  -  Arrival Sensor OFF\n");
			//making sure the movement is synchronizing with the scheduler
			synchronized(s)
			{
				if(curr_Floor < request.getFloorDestination())
				{
					stateMachine(ElevatorStates.UP_STATE);
					System.out.println(Timestamp.from(Instant.now()) + "  -  Lamp Number " + curr_Floor);
				}
				if(curr_Floor > request.getFloorDestination())
				{
					stateMachine(ElevatorStates.DOWN_STATE);
					System.out.println(Timestamp.from(Instant.now()) + "  -  Lamp Number " + curr_Floor);
				}
			}
		}
		//arrive at floor open door and turn off lamp
		openDoor();
		lampOff();
		System.out.println(Timestamp.from(Instant.now()) + "  -    ****DOOR OPENED****");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ~~~~ARRIVED AT FLOOR " + curr_Floor + "~~~~");
		System.out.println(Timestamp.from(Instant.now()) + "  -  Arrival Sensor ON");
		//state = elevatorStates.STOP_STATE;
		stateMachine(ElevatorStates.STOP_STATE);
		
		System.out.println(Timestamp.from(Instant.now()) + "  -  Arrival Sensor OFF");
		System.out.println("Waiting for next reqest....");
		
		return true;
	}
	
	/*
	 * If there is a button pressed in the elevator it will call the operate_check function
	 * 
	 * @params ArrayList<FloorRequest> request - will be used in the function to check whether there are any requested floors on the list and will let the operate_check function read it
	 */
	public void button_pressed(FloorRequest request)
	{
		System.out.println(Timestamp.from(Instant.now()) + "  -  Button Pressed");
		//run the number through the operate check function
		this.buttons[request.getFloorDestination() - 1] = true;
		operate(request);
		stop();
		openDoor();
		//make the button false after arriving to the floor
		this.buttons[request.getFloorDestination()] = false;
	}
	
	/*
	 * These functions will control the elevator motor movement, up, down, and stop
	 */
	public void go_Up()
	{
		this.motor = Elevator_Motor.Up;
		curr_Floor++;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: GOING UP");
		try {
			System.out.println("Travelling to the requested floor....Have Patience");
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void go_Down()
	{
		this.motor = Elevator_Motor.Down;
		curr_Floor--;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: GOING DOWN");
		try {
			System.out.println("Travelling to the requested floor....Have Patience");
			Thread.sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public synchronized void stop()
	{
		this.motor = Elevator_Motor.Stop;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: STOPPED\n");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Floor reached. Unloading\n");
		try {
			Thread.sleep(2000);
			System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Doors opened.\n");
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * These additional functions will be used in the test class to make sure that everything is functioning properly
	 */
	public Elevator_Motor getDirection()
	{
		return this.motor;
	}
	
	public boolean lampStatus()
	{
		return this.lamp_Status;
	}
	
	public void lampOn()
	{
		lamp_Status = true;
	}
	
	public void lampOff()
	{
		lamp_Status = false;
	}
	
	public boolean doorStatus()
	{
		return this.open_Door;
	}
	
	public void closeDoor()
	{
		open_Door = false; 
	}
	
	public void openDoor()
	{
		open_Door = true; 
	}
	
	/*
	 * This function will just be used to run the elevator class when it is called in the main class
	 */
	@Override
	public void run()
	{
		while(true)
		{
			stateMachine(ElevatorStates.IDLE_STATE);
			synchronized(s)
			{
				operate(s.getRequest());
			}
		}
	}
}
