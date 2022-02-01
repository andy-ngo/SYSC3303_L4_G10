
public class Elevator implements Runnable 
{

	private Floor f;
	private Scheduler s;
	private boolean open_Door = false;
	private boolean lamp = false;
	private int lamp_Num = 0;
	
	public Elevator(Floor f, boolean open_Door)
	{
		this.f = f;
		this.open_Door = open_Door;
	}
	
	public boolean check()
	{
		if(lamp == false && f.pressed())
		{
			//go to floor
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
	
	public void run()
	{
		
	}
}
