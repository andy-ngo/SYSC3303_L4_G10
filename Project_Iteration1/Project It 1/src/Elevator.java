/*
 * Author: Andy Ngo
 * Student ID: 101132278
 */

import java.util.ArrayList;

public class Elevator implements Runnable 
{

	private Scheduler s;
	private Elevator_Motor motor;
	private int tot_Floors = 13;
	private int id;
	private boolean buttons[];
	private boolean open_Door = false;
	private boolean lamp = false;
	private int lamp_Num = 0;
	private int curr_Floor = 0;
	
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
		buttons = new boolean[tot_Floors];
		
	}
	
	public boolean operate_Check(ArrayList<FloorRequest> request)
	{
		//go to floor
		open_Door = false;
		for(int i = 0; i < request.size(); i++)
		{
			lamp = true;
			synchronized(s)
			{
				if(curr_Floor < request.get(i).getFloorDestination())
				{
					go_Up();
					curr_Floor++;
					System.out.println("Lamp Number" + curr_Floor);
				}
				if(curr_Floor > request.get(i).getFloorDestination())
				{
					go_Down();
					curr_Floor--;
					System.out.println("Lamp Number" + curr_Floor);
				}
			}
			
		}	
			//get to floor
		lamp = false;
		open_Door = true;
		
		return true;
	}
	
	public void button_pressed(ArrayList<FloorRequest> request)
	{
		//will check if there is another floor button on queue and will go to the closest floor
		operate_Check(request);
		//button will turn off
		///this.buttons[button_Num] = false;
		stop();
		lamp = false;
		open_Door = true;
		
	}
	
	private boolean go_Up()
	{
		this.motor = Elevator_Motor.Up;
		System.out.println("Going up");
		notifyAll();
		return true;
	}
	
	private boolean go_Down()
	{
		this.motor = Elevator_Motor.Down;
		System.out.println("Going down");
		notifyAll();
		return true;
	}
	
	private synchronized boolean stop()
	{
		this.motor = Elevator_Motor.Stop;
		s.putArrivalSensor(curr_Floor,true);
		System.out.println("Floor reached");
		notifyAll();
		return true;
	}
	
	private boolean closeDoor()
	{
		this.open_Door = false;
		return true;
	}
	
	public int getCurrentFloor()
	{
		return this.curr_Floor;
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			synchronized(s)
			{
				if(operate_Check(s.getRequests()))
				{
					button_pressed(s.getRequests());
				}
				else
				{
					s.notifyAll();
				}
			}
		}
	}
}
