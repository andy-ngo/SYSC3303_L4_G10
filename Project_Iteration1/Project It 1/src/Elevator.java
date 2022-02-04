
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
	
	public Elevator(Scheduler scheduler, int id, int curr_Floor, boolean open_Door)
	{
		this.scheduler = scheduler;
		this.curr_Floor = 1;
		this.lamp_Num = curr_Floor;
		this.open_Door = open_Door;
		this.motor = Elevator_Motor.Stop;
		buttons = new boolean[tot_Floors];
		
	}
	
	public boolean operate_Check()
	{
		if(lamp == false && s.request() == true)
		{
			//go to floor
			while(curr_Floor != s.get_Floor())
			{
				synchronized(s)
				{
					lamp = true;
					if(curr_Floor < s.get_Floor())
					{
						go_Up();
						curr_Floor++;
						s.floor_Change(getCurrentFloor());
						System.out.println("Lamp Number" + curr_Floor);
					}
					if(curr_Floor > s.get_Floor())
					{
						go_Down();
						curr_Floor--;
						s.floor_Change(getCurrentFloor());
						System.out.println("Lamp Number" + curr_Floor);
					}
				}
			}	
			//get to floor
			lamp = false;
			open_Door = true;
			return true;
		}
		return false;
	}
	
	public void button_pressed(int button_Num)
	{
		//will check if there is another floor button on queue and will go to the closest floor
		this.buttons[button_Num-1] = true;
		//schedule class will add the button number to the queue for the next floor it will go to
		s.next_Floor(button_Num);
		
		while(curr_Floor != s.get_Floor)
		{
			lamp = true;
			synchronized(s)
			{
				if(curr_Floor < s.get_Floor)
				{
					go_Up();
					curr_Floor++;
					s.floor_Change(getCurrentFloor());
					System.out.println("Lamp Number" + curr_Floor);
				}
				if(curr_Floor > s.get_Floor)
				{
					go_Down();
					curr_Floor--;
					s.floor_Change(getCurrentFloor());
					System.out.println("Lamp Number" + curr_Floor);
				}
			}
			
		}
		//button will turn off
		this.buttons[button_Num] = false;
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
		s.arrival();
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
				if(operate_Check())
				{
					button_pressed(s.request_Floor());
				}
				else
				{
					s.notifyAll();
				}
			}
		}
	}
}
