
public class Elevator implements Runnable 
{

	private Scheduler s;
	public enum motor_Movement{down,stop,up};
	private motor_Movement motor;
	private int id = 0;
	private boolean open_Door = false;
	private boolean lamp = false;
	private int lamp_Num = 0;
	private int curr_Floor = 0;
	
	public Elevator(Scheduler s, boolean open_Door)
	{
		this.s = s;
		this.open_Door = open_Door;
	}
	
	public boolean check()
	{
		if(lamp == false && s.request() == true)
		{
			//go to floor
			while(lamp_Num != s.get_Floor())
			{
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
			open_Door = true;
			return true;
		}
		return false;
	}
	
	public void button_pressed(Floor f)
	{
		//will check if there is another floor button on queue and will go to the closest floor
		s.next_floor();
		
		
	}
	
	private void go_Up()
	{
		motor.up;
		System.out.println("Going up");
	}
	
	private void go_Down()
	{
		motor.down;
		System.out.println("Going down");
	}
	
	private void stop()
	{
		motor.stop;
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
