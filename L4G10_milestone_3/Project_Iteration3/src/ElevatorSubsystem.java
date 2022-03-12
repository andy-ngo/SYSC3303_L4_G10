/**
 * @author Andy Ngo, Karim Mahrous
 * Version: 3.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * A state machine is implemented to organize some parts of the code.
 * This class will be synchronizing with the scheduler class.
 */

import java.net.*;
import java.io.*;
import java.sql.Timestamp;
import java.time.Instant;

public class ElevatorSubsystem implements Runnable 
{
	//initialize variables
	private Scheduler s;
	private Elevator_Motor motor;
	private int id;
	private boolean buttons[];
	private boolean open_Door;
	private boolean lamp_Status;
	private boolean busy;
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
		this.busy = false;
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

	/**
	 * This method is used to initialize the UDP
	 */
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
			case IDLE:
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
				System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Waiting for requests...\n");
				break;
			
			case OPERATE:
				System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: ~~Operate State~~");
				break;
			//go up
			case UP:
				go_Up();
				/*
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				break;
				
			//go down
			case DOWN:
				go_Down();
				/*
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				break;

			//stop/unloading
			case STOP:
				stop();
				/*
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				/*
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
				System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: notifying Scheduler of arrival...");
				s.putArrivalSensor(id, curr_Floor);

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


	
	 /**
	  * This function will check whether the floor that the elevator is trying to get to is higher or lower and will keep going until it reaches the destination floor
	  * @param request - will be used in the function to check whether there are any requested floors on the list.
	  * @return true
	  */
	public boolean operate(FloorRequest request)
	{
		
		stateMachine(ElevatorStates.OPERATE);
		
		//initialize variables to use in the loop
		lampOn();
		closeDoor();

		System.out.println("\nELEVATOR: Waiting for next request....");
		s.printPacket();
		System.out.println("ELEVATOR: Packet recieved from Floor Subsystem....");
		System.out.println("ELEVATOR: Parsing packet.....");
		System.out.println("ELEVATOR: Parsing complete processing request....\n");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Going to next request");

		//will check if there are any more floors on the request list and will either go up or down depending on current floor
		if(curr_Floor < request.getFloorOrigin())
		{
			go_Up();
			System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: To the next requested floor: " + request.getFloorOrigin());
		}
		else if(curr_Floor > request.getFloorOrigin())
		{
			go_Down();
			System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: To the next requested floor: " + request.getFloorOrigin());
		}
		
		curr_Floor = request.getFloorOrigin();
		stop();
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Loading passengers...");
		
		//print out the current floor and destination floor
		System.out.println(Timestamp.from(Instant.now()) + "  -  ###########################");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ##   Current Floor: " + curr_Floor + "   ##");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ## Destination Floor: " + request.getFloorDestination()+ " ##");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ###########################");
		
		//keep checking if current floor is the same as the destination floor or else keep looping
		while(curr_Floor != request.getFloorDestination() )
		{
			System.out.println(Timestamp.from(Instant.now()) + "  -  ======= ELEVATOR " + id + " =======");
			System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Arrival Sensor OFF");
			//making sure the movement is synchronizing with the scheduler
			synchronized(s)
			{
				if(curr_Floor < request.getFloorDestination())
				{
					stateMachine(ElevatorStates.UP);
					System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Lamp Number " + curr_Floor);
				}
				if(curr_Floor > request.getFloorDestination())
				{
					stateMachine(ElevatorStates.DOWN);
					System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Lamp Number " + curr_Floor);
				}
			}
		}
		//arrive at floor open door and turn off lamp
		openDoor();
		lampOff();
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR:****DOOR OPENED****");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ~~~~ARRIVED AT FLOOR " + curr_Floor + "~~~~");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Arrival Sensor ON");
		stateMachine(ElevatorStates.STOP);
		
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Arrival Sensor OFF");
		
		return true;
	}
	
	/**
	 * If there is a button pressed in the elevator it will call the operate_check function
	 * 
	 * @param request - will be used in the function to check whether there are any requested floors on the list and will let the operate_check function read it
	 */
	public void button_pressed(FloorRequest request)
	{
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Button Pressed");
		//run the number through the operate check function
		this.buttons[request.getFloorDestination() - 1] = true;
		operate(request);
		stop();
		openDoor();
		//make the button false after arriving to the floor
		this.buttons[request.getFloorDestination()] = false;
	}
	
	/**
	 * This function will control the elevator motor movement up
	 */
	public void go_Up()
	{
		this.motor = Elevator_Motor.Up;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: GOING UP");
		curr_Floor++;
		//System.out.println("Travelling to the requested floor....Have Patience");
		
	}
	
	/**
	 * This function will control the elevator motor movement down
	 */
	public void go_Down()
	{
		this.motor = Elevator_Motor.Down;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: GOING DOWN");
		curr_Floor--;
		//System.out.println("Travelling to the requested floor....Have Patience");
		
	}
	
	/**
	 * This function will stop the elevator
	 */
	public synchronized void stop()
	{
		this.motor = Elevator_Motor.Stop;
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: STOPPED\n");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Floor reached.\n");
		System.out.println(Timestamp.from(Instant.now()) + "  -  ELEVATOR: Doors opened.\n");
	}
	/**
	 * This function gets the direction of the elevator motor
	 * @return Elevator_Motor - the direction of the elevator motor
	 */
	public Elevator_Motor getDirection()
	{
		return this.motor;
	}
	
	/**
	 * This will return the lamp status
	 * @return boolean - if true the lamp is on, if false the lamp is off
	 */
	public boolean lampStatus()
	{
		return this.lamp_Status;
	}
	/**
	 * Will set lamp status as true to be on
	 */
	public void lampOn()
	{
		lamp_Status = true;
	}
	
	/**
	 * Will set lamp status as false to be off
	 */
	public void lampOff()
	{
		lamp_Status = false;
	}
	
	/**
	 * Will return the status of the door
	 * @return boolean - will return true if the door is open and false for closed
	 */
	public boolean doorStatus()
	{
		return this.open_Door;
	}
	
	/**
	 * Will return a status of the elevator if it is busy
	 * @return boolean - will be true if it is busy and false if it is not
	 */
	public boolean getBusyStatus()
	{
		return this.busy;
	}
	
	/**
	 * Will set the door status as false to be closed
	 */
	public void closeDoor()
	{
		open_Door = false; 
	}
	 
	/**
	 * Will set the door status as true to be open
	 */
	public void openDoor()
	{
		open_Door = true; 
	}
	
	/**
	 * Will get the current floor
	 * @return int - return the floor
	 */
	public int getCurrFloor()
	{
		return this.curr_Floor;
	}
	
	/**
	 * Will get the elevator motor
	 * @return Elevator Motor - the motor direction
	 */
	public Elevator_Motor getMotor()
	{
		return this.motor;
	}
	
	/**
	 * This function will just be used to run the elevator class when it is called in the main class
	 */
	@Override
	public void run()
	{
		while(true)
		{
			stateMachine(ElevatorStates.IDLE);
			synchronized(s)
			{
				operate(s.getRequest(0));
			}
		}
	}
}
