/*
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 2.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * A state machine is implemented to organize some parts of the code.
 * This class will be synchronizing with the scheduler class.
 */

import java.util.ArrayList;

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
	
	//initialize constructors
	public ElevatorSubsystem(Scheduler s)
	{
		this.s = s;
	}
	
	public ElevatorSubsystem(int id, int curr_Floor)
	{
		this.id = id;
		this.open_Door = false;
		this.lamp_Status = false;
	}
	
	/**
	 * This will be the state machine controlling the elevator movement by following the state given
	 * @param int state - will be used to change the state
	 */
	public void stateMachine(int state)
	{
		switch(state)
		{
			//idle
			case 0:
				System.out.println("Waiting for requests...\n");
				break;
			
			//operate
			case 1:
				System.out.println("\n~~Operate State~~");
				//operate(s.getRequests());
				break;
			//go up
			case 2:
				System.out.println("\nELEVATOR GOING UP\n");
				go_Up();
				break;
				
			//go down
			case 3:
				System.out.println("\nELEVATOR GOING DOWN\n");
				go_Down();
				break;

			//stop/unloading
			case 4:
				System.out.println("\nELEVATOR STOPPED\n");
				stop();
				s.setElevatorArrival(id, curr_Floor);
				//state = elevatorStates.OPERATE_STATE;
				break;
				
		}
		
	}

	/*
	 * This function will check whether the floor that the elevator is trying to get to is higher or lower and will keep going until it reaches the destination floor
	 * 
	 * @param ArrayList<FloorRequest> request - will be used in the function to check whether there are any requested floors on the list.
	 */
	public boolean operate(ArrayList<FloorRequest> request)
	{
		
		stateMachine(1);
		
		//go through floor request list
		for(int i = 0 ; i < request.size(); i++)
		{
			//initialize variables to use in the loop
			lampOn();
			closeDoor();
			
			
			int nextFloor = 0;
			id = 1;
			curr_Floor = request.get(i).getFloorOrigin();
			
			//set nextFloor as the floor the request came from as long as there is a next requested floor
			if(i <= request.size()-2)
			{
				nextFloor = request.get(i+1).getFloorOrigin();
			}

			s.setElevatorArrival(id, curr_Floor);
			
			//print out the current floor and destination floor
			System.out.println("\n###########################");
			System.out.println("\n##   Current Floor: " + curr_Floor + "   ##");
			System.out.println("\n## Destination Floor: " + request.get(i).getFloorDestination()+ " ##");
			System.out.println("\n###########################");
			
			//keep checking if current floor is the same as the destination floor or else keep looping
			while(curr_Floor != request.get(i).getFloorDestination() )
			{
				System.out.println("\n======= ELEVATOR " + id + " =======");
				System.out.println("Arrival Sensor OFF");
				//making sure the movement is synchronizing with the scheduler
				synchronized(s)
				{
					if(curr_Floor < request.get(i).getFloorDestination())
					{
						stateMachine(2);
						System.out.println("Lamp Number " + curr_Floor);
					}
					if(curr_Floor > request.get(i).getFloorDestination())
					{
						stateMachine(3);
						System.out.println("Lamp Number " + curr_Floor);
					}
				}
			}
			//arrive at floor open door and turn off lamp
			openDoor();
			lampOff();
			System.out.println("\n  ****DOOR OPENED****");
			System.out.println("~~~~ARRIVED AT FLOOR " + curr_Floor + "~~~~");
			System.out.println("Arrival Sensor ON");
			//state = elevatorStates.STOP_STATE;
			stateMachine(4);
				
			//will print out no more requests once i reaches it's limit
			if(i == request.size()-1)
			{
				System.out.println("\nNo more requests\n");
			}
			
			//will change the status of the arrival sensor to update the floor subsystem through the scheduler
			s.putArrivalSensor(curr_Floor,true);
			
			//will check if there are any more floors on the request list and will either go up or down depending on current floor
			if(curr_Floor < nextFloor)
			{
				System.out.println("\nGoing to next request");
				go_Up();
			}
			else if(curr_Floor > nextFloor)
			{
				System.out.println("\nGoing to next request");
				go_Down();
			}
			
		}
		System.out.println("Arrival Sensor OFF");
		s.putArrivalSensor(curr_Floor,false);
		return true;
	}
	
	/*
	 * If there is a button pressed in the elevator it will call the operate_check function
	 * 
	 * @params ArrayList<FloorRequest> request - will be used in the function to check whether there are any requested floors on the list and will let the operate_check function read it
	 */
	public void button_pressed(ArrayList<FloorRequest> request)
	{
		System.out.println("Button Pressed");
		//run the number through the operate check function
		this.buttons[request.get(request.size()).getFloorDestination() - 1] = true;
		operate(request);
		stop();
		openDoor();
		//make the button false after arriving to the floor
		this.buttons[request.get(request.size()).getFloorDestination()] = false;
	}
	
	/*
	 * These functions will control the elevator motor movement, up, down, and stop
	 */
	public void go_Up()
	{
		this.motor = Elevator_Motor.Up;
		curr_Floor++;
		System.out.println("Going up");
	}
	
	public void go_Down()
	{
		this.motor = Elevator_Motor.Down;
		curr_Floor--;
		System.out.println("Going down");
	}
	
	public synchronized void stop()
	{
		this.motor = Elevator_Motor.Stop;
		System.out.println("Floor reached\n");
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
			stateMachine(0);
			synchronized(s)
			{
				operate(s.getRequests());
			}
		}
	}
}
