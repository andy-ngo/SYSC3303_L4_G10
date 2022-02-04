/*
 * Author: Andy Ngo
 * Student ID: 101132278
 * Version: 1.0V
 * 
 * Description:
 * The purpose of this class is for the elevator thread and it will check the floor request array and make sure
 * if there are any requests. If there are requests the elevator will check to go up and down or to stop when it arrives.
 * This class will be synchronizing with the scheduler class.
 */

import java.util.ArrayList;

public class Elevator implements Runnable 
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
	public Elevator(Scheduler s)
	{
		this.s = s;
	}
	
	public Elevator(int id, int curr_Floor)
	{
		this.id = id;
		this.curr_Floor = 1;
		this.open_Door = false;
		this.motor = Elevator_Motor.Stop;
		this.lamp_Status = false;
	}
	
	/*
	 * This function will check whether the floor that the elevator is trying to get to is higher or lower and will keep going until it reaches the destination floor.
	 * 
	 * @params ArrayList<FloorRequest> request
	 * The parameter will be used in the function to check whether there are any requested floors on the list.
	 */
	public boolean operate_Check(ArrayList<FloorRequest> request)
	{
		//go to floor
		closeDoor();
		for(int i = 0 ; i < request.size(); i++)
		{
			lamp_Status = true;
			this.id = i;
			curr_Floor = request.get(i).getFloorOrigin();
			s.putArrivalSensor(curr_Floor,false);
			
			//keep checking if current floor is the same as the destination floor or else keep looping
			while(curr_Floor != request.get(i).getFloorDestination())
			{
				System.out.println("\n======= ELEVATOR " + id + " =======");
				
				//making sure the movement is synchronizing with the scheduler
				synchronized(s)
				{
					if(curr_Floor < request.get(i).getFloorDestination())
					{
						go_Up();
						curr_Floor++;
						System.out.println("Lamp Number " + curr_Floor);
					}
					if(curr_Floor > request.get(i).getFloorDestination())
					{
						go_Down();
						curr_Floor--;
						System.out.println("Lamp Number " + curr_Floor);
					}
				}
			}
			//arrive at floor
			open_Door = true;
			lamp_Status = false;
			System.out.println("\n  ****DOOR OPENED****");
			System.out.println("~~~~ARRIVED AT FLOOR " + curr_Floor + "~~~~");
			s.putArrivalSensor(curr_Floor,true);
			stop();
		}	
		return true;
	}
	
	/*
	 * If there is a button pressed in the elevator it will call the operate_check function
	 * 
	 * @params ArrayList<FloorRequest> request
	 * The parameter will be used in the function to check whether there are any requested floors on the list and will let the operate_check function read it
	 */
	public void button_pressed(ArrayList<FloorRequest> request)
	{
		System.out.println("Button Pressed");
		//run the number through the operate check function
		//this will make requested number pressed true
		this.buttons[request.get(request.size()-1).getFloorDestination() - 1] = true;
		operate_Check(request);
		stop();
		openDoor();
		//make the button false after arriving to the floor
		this.buttons[request.get(request.size()-1).getFloorDestination()] = false;
	}
	
	/*
	 * These functions will control the elevator motor movement, up, down, and stop
	 */
	public void go_Up()
	{
		this.motor = Elevator_Motor.Up;
		System.out.println("Going up");
	}
	
	public void go_Down()
	{
		this.motor = Elevator_Motor.Down;
		System.out.println("Going down");
	}
	
	public synchronized void stop()
	{
		this.motor = Elevator_Motor.Stop;
		System.out.println("Floor reached");
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
			synchronized(s)
			{
				operate_Check(s.getRequests());
			}
		}
	}
}
