
public enum Elevator_Motor{

	Up
	{
		@Override
		public String toString()
		{
			return "up";
		}
	},
	
	Stop
	{
		@Override
		public String toString()
		{
			return "stop";
		}
	},
	
	Down
	{
		@Override
		public String toString()
		{
			return "down";
		}
	};
	
}
