
public class Elevator implements Runnable 
{

	private Scheduler s;
	private Elevator_Motor motor;
	private int tot_Floors = 13;
	private int id;
	
	private boolean open_Door = false;
	private boolean lamp = false;
	private int lamp_Num = 0;
	private int curr_Floor = 0;
	
	public Elevator(Scheduler s, boolean open_Door)
	{
		this.s = s;
		this.curr_Floor = 1;
		this.lamp_Num = curr_Floor;
		this.open_Door = false;
		this.motor = Elevator_Motor.Stop;
		buttons = new boolean[tot_Floors];
	}
	
	public boolean check()
	{
		if(lamp == false && s.request() == true)
		{
			//go to floor
			while(lamp_Num != s.get_Floor())
			{
				lamp = true;
				if(lamp_Num < s.get_Floor())
				{
					go_Up();
					lamp_Num++;
					return true;
				}
				if(lamp_Num > s.get_Floor())
				{
					go_Down();
					lamp_Num--;
					return true;
				}
			}	
			//get to floor
			lamp = false;
			open_Door = true;
			return true;
		}
		return false;
	}
	
	public void button_pressed(boolean buttons[])
	{
		//will check if there is another floor button on queue and will go to the closest floor
		s.next_floor();

	}
	
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
	
	private void stop()
	{
		this.motor = Elevator_Motor.Stop;
		System.out.println("Floor reached");
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			synchronized(s)
			{
				
			}
		}
	}
}
