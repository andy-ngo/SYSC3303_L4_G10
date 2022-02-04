/*
 * Author: Andy Ngo
 * Student ID: 101132278
 */

import java.util.ArrayList;

public class Elevator implements Runnable 
{
	//initialize variables
	private Scheduler s;
	private Elevator_Motor motor;
	private boolean buttons[];
	private boolean open_Door = false;
	private int lamp_Num;
	private int curr_Floor;
	
	//initialize constructors
	public Elevator(Scheduler s)
	{
		this.s = s;
	}
	
	public Elevator(Scheduler s,int id, int curr_Floor, boolean open_Door)
	{
		this.s = s;
		this.curr_Floor = 1;
		this.lamp_Num = curr_Floor;
		this.open_Door = open_Door;
		this.motor = Elevator_Motor.Stop;
	}
	
	//this function will check whether the floor that the elevator is trying to get to is higher or lower and will keep going until it reaches the destination floor
	public boolean operate_Check(ArrayList<FloorRequest> request)
	{
		//go to floor
		open_Door = false;
		for(int i = 0 ; i < request.size(); i++)
		{
			curr_Floor = request.get(i).getFloorOrigin();
			while(curr_Floor != request.get(i).getFloorDestination())
			{
				System.out.println("\n======= ELEVATOR " + i + " =======");
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
			System.out.println("\n  ****DOOR OPENED****");
			System.out.println("~~~~ARRIVED AT FLOOR " + curr_Floor + "~~~~");
			stop();
		}	
		return true;
	}
	
	//if there is a button pressed in the elevator it will call the operate_check funtion
	public void button_pressed(ArrayList<FloorRequest> request)
	{
		System.out.println("Button Pressed");
		//run the number through the operate check function
		operate_Check(request);
		stop();
		open_Door = true;
		
	}
	
	//These functions will control the elevator motor movement, up, down, and stop
	private void go_Up()
	{
		this.motor = Elevator_Motor.Up;
		System.out.println("Going up");
	}
	
	private void go_Down()
	{
		this.motor = Elevator_Motor.Down;
		System.out.println("Going down");
	}
	
	private synchronized void stop()
	{
		this.motor = Elevator_Motor.Stop;
		s.putArrivalSensor(curr_Floor,true);
		System.out.println("Floor reached");
	}
	
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
